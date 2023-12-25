package ru.handh.project.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.dto.SimpleTelegramTokenDto
import ru.handh.project.dto.TelegramTokenDto
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.AuthRequest
import ru.handh.project.dto.request.DeleteAccountRequest
import ru.handh.project.dto.request.RefreshRequest
import ru.handh.project.dto.request.RegisterRequest
import ru.handh.project.dto.request.SignOutRequest
import ru.handh.project.entity.TelegramTokenEntity
import ru.handh.project.entity.UserEntity
import ru.handh.project.exception.ApiError
import ru.handh.project.model.UserDeletedEvent
import ru.handh.project.repository.TelegramTokenRepository
import ru.handh.project.repository.UserRepository
import java.util.UUID

private val log = KotlinLogging.logger {}

@Service
class UserService(
    @Value("\${rsa.isEncrypted}")
    private val isEncrypted: Boolean,

    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val telegramTokenRepository: TelegramTokenRepository,
    private val tokenService: TokenService,
    private val signatureService: SignatureService,
    private val outboxMessageService: OutboxMessageService
) {

    @Transactional
    fun register(registerRequest: RegisterRequest) =
        registerRequest.toEntity()
            .let {
                userRepository.save(it)
            }
            .run {
                log.info { "save new User{id=${id}}" }
                tokenService.getToken(this, isEncrypted, signatureService.signatureType)
            }

    @Transactional
    fun auth(authRequest: AuthRequest) =
        userRepository.findByUsername(authRequest.username!!)
            ?.checkPasswordMatch(authRequest.password!!)
            ?.run {
                log.debug { "successful user authentication: id=${id}" }
                tokenService.getToken(this, isEncrypted, signatureService.signatureType)
            }
            ?: throw ApiError.USERNAME_NOT_FOUND.toException(authRequest.username)

    fun refresh(userId: Int, refreshRequest: RefreshRequest) =
        tokenService.refresh(userId, refreshRequest.jwtId!!, isEncrypted, signatureService.signatureType)

    fun signOut(signOutRequest: SignOutRequest) =
        tokenService.signOut(signOutRequest.jwtId!!)

    @Transactional
    fun deleteAccount(userId: Int, deleteAccountRequest: DeleteAccountRequest) =
        userRepository.findByIdOrNull(userId)
            ?.checkPasswordMatch(deleteAccountRequest.password!!)
            ?.run {
                log.info { "User{id=$id} was deleted" }
                userRepository.deleteById(id)
            }
            ?.also {
                outboxMessageService.saveMessage(UserDeletedEvent(DeletedMessageDto(userId)))
            }
            ?: throw ApiError.USER_NOT_FOUND.toException(userId)

    fun getTelegramToken(userId: Int): SimpleTelegramTokenDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw ApiError.USER_NOT_FOUND.toException(userId)
        val telegramToken = telegramTokenRepository.findByUserId(user.id)
            ?: let {
                log.debug { "generate telegram token for user{id=${user.id}}" }
                telegramTokenRepository.save(
                    TelegramTokenEntity(userId = user.id, token = UUID.randomUUID().toString())
                )
            }
        return SimpleTelegramTokenDto(token = telegramToken.token)
    }

    fun authByTelegramToken(telegramToken: SimpleTelegramTokenDto): TelegramTokenDto {
        telegramTokenRepository.findByToken(telegramToken.token!!)
            ?.run {
                log.debug { "successful user authentication by telegram token: id=${this.userId}" }
                return TelegramTokenDto(token = this.token, userId = this.userId)
            }
            ?: throw ApiError.TELEGRAM_TOKEN_NOT_FOUND.toException(telegramToken.token)
    }

    private fun RegisterRequest.toEntity(id: Int = -1) =
        UserEntity(
            id = id,
            name = name!!,
            username = username!!,
            password = passwordEncoder.encode(password)
        )

    private fun UserEntity.checkPasswordMatch(rawPassword: String) =
        if (!passwordEncoder.matches(rawPassword, password))
            throw ApiError.WRONG_PASSWORD.toException()
        else this
}

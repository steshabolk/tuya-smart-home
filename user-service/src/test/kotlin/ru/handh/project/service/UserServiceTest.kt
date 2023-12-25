package ru.handh.project.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.dto.SimpleTelegramTokenDto
import ru.handh.project.dto.request.AuthRequest
import ru.handh.project.dto.request.DeleteAccountRequest
import ru.handh.project.dto.request.RegisterRequest
import ru.handh.project.entity.TelegramTokenEntity
import ru.handh.project.entity.UserEntity
import ru.handh.project.enum.DomainEventType
import ru.handh.project.enum.Signature
import ru.handh.project.exception.ApiError
import ru.handh.project.model.UserDeletedEvent
import ru.handh.project.repository.TelegramTokenRepository
import ru.handh.project.repository.UserRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.deleteRequest
import ru.handh.project.util.expectApiError
import ru.handh.project.util.refreshRequest
import ru.handh.project.util.refreshToken
import ru.handh.project.util.signOutRequest
import ru.handh.project.util.simpleTelegramTokenDto
import ru.handh.project.util.telegramToken
import ru.handh.project.util.tokenDto
import ru.handh.project.util.userDeletedMessage
import ru.handh.project.util.userEntity
import ru.handh.project.util.userId
import ru.handh.project.util.userIdNotFound
import ru.handh.project.util.userName
import ru.handh.project.util.userPassword
import ru.handh.project.util.userPasswordNotMatch
import ru.handh.project.util.userUsername
import ru.handh.project.util.usernameNotFound

@Import(UserService::class, BCryptPasswordEncoder::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class UserServiceTest {

    @Autowired
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @MockkBean
    private lateinit var httpSecurity: HttpSecurity

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @SpykBean
    private lateinit var userRepository: UserRepository

    @SpykBean
    private lateinit var telegramTokenRepository: TelegramTokenRepository

    @MockkBean
    private lateinit var tokenService: TokenService

    @MockkBean
    private lateinit var signatureService: SignatureService

    @MockkBean
    private lateinit var outboxMessageService: OutboxMessageService

    @Autowired
    private lateinit var userService: UserService

    private val userEntitySlot = slot<UserEntity>()
    private val userDeletedEventSlot = slot<UserDeletedEvent>()
    private val telegramTokenSlot = slot<TelegramTokenEntity>()

    @Nested
    inner class Register {

        private val request = RegisterRequest(
            name = userName,
            username = userUsername,
            password = userPassword,
            confirmPassword = userPassword
        )

        @Test
        fun success() {

            every { userRepository.save(capture(userEntitySlot)) } answers { callOriginal() }
            every { signatureService.signatureType } answers { Signature.SECRET }
            every { tokenService.getToken(capture(userEntitySlot), any(), any()) } answers { tokenDto }

            userService.register(request)

            assertTrue(userRepository.existsById(userEntitySlot.captured.id))
            assertEquals(userName, userEntitySlot.captured.name)
            assertEquals(userUsername, userEntitySlot.captured.username)
            assertTrue(passwordEncoder.matches(userPassword, userEntitySlot.captured.password))
        }
    }

    @Nested
    inner class Auth {

        @Test
        fun success() {

            val request = AuthRequest(
                username = userUsername,
                password = userPassword
            )

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { signatureService.signatureType } answers { Signature.SECRET }
            every { tokenService.getToken(capture(userEntitySlot), any(), any()) } answers { tokenDto }

            userService.auth(request)

            verify(exactly = 1) { userRepository.findByUsername(savedUser.username) }

            assertEquals(savedUser.id, userEntitySlot.captured.id)
            assertEquals(userUsername, userEntitySlot.captured.username)
            assertTrue(passwordEncoder.matches(userPassword, userEntitySlot.captured.password))
        }

        @Test
        fun `should throw an exception if user is not found`() {

            val request = AuthRequest(
                username = usernameNotFound,
                password = userPassword
            )

            assertFalse(userRepository.existsByUsername(usernameNotFound))

            expectApiError(ApiError.USERNAME_NOT_FOUND) {
                userService.auth(request)
            }
        }

        @Test
        fun `should throw an exception if users password does not match`() {

            val request = AuthRequest(
                username = userUsername,
                password = userPasswordNotMatch
            )

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))
            assertFalse(passwordEncoder.matches(userPasswordNotMatch, savedUser.password))

            expectApiError(ApiError.WRONG_PASSWORD) {
                userService.auth(request)
            }
        }
    }

    @Nested
    inner class RefreshToken {

        @Test
        fun success() {

            every { signatureService.signatureType } answers { Signature.SECRET }
            every { tokenService.refresh(userId, refreshToken, any(), any()) } answers { tokenDto }

            userService.refresh(userId, refreshRequest)

            verify(exactly = 1) { tokenService.refresh(userId, refreshToken, any(), any()) }
        }
    }

    @Nested
    inner class SignOut {

        @Test
        fun success() {

            every { tokenService.signOut(refreshToken) } just Runs

            userService.signOut(signOutRequest)

            verify(exactly = 1) { tokenService.signOut(refreshToken) }
        }
    }

    @Nested
    inner class DeleteAccount {

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { outboxMessageService.saveMessage(capture(userDeletedEventSlot)) } returns userDeletedMessage

            userService.deleteAccount(savedUser.id, deleteRequest)

            assertFalse(userRepository.existsById(savedUser.id))

            assertEquals(DomainEventType.USER_DELETED, userDeletedEventSlot.captured.type)
            assertEquals(savedUser.id, userDeletedEventSlot.captured.message.id)
            verify(exactly = 1) { outboxMessageService.saveMessage(userDeletedEventSlot.captured) }

            verify(exactly = 1) { userRepository.findById(savedUser.id) }
            verify(exactly = 1) { userRepository.deleteById(savedUser.id) }
        }

        @Test
        fun `should throw an exception if user is not found`() {

            assertFalse(userRepository.existsById(userIdNotFound))

            expectApiError(ApiError.USER_NOT_FOUND) {
                userService.deleteAccount(userIdNotFound, deleteRequest)
            }
        }

        @Test
        fun `should throw an exception if users password does not match`() {

            val request = DeleteAccountRequest(
                password = userPasswordNotMatch
            )

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))
            assertFalse(passwordEncoder.matches(userPasswordNotMatch, savedUser.password))


            expectApiError(ApiError.WRONG_PASSWORD) {
                userService.deleteAccount(savedUser.id, request)
            }
        }
    }

    @Nested
    inner class GetTelegramToken {

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { telegramTokenRepository.save(capture(telegramTokenSlot)) } answers { callOriginal() }

            val telegramToken = userService.getTelegramToken(savedUser.id)

            verify(exactly = 1) { userRepository.findById(savedUser.id) }
            verify(exactly = 1) { telegramTokenRepository.findByUserId(savedUser.id) }
            verify(exactly = 1) { telegramTokenRepository.save(telegramTokenSlot.captured) }

            assertEquals(savedUser.id, telegramTokenSlot.captured.userId)
            assertEquals(telegramToken.token, telegramTokenSlot.captured.token)
        }

        @Test
        fun `success with existing token`() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))
            val telegramTokeEntity = TelegramTokenEntity(
                id = -1,
                userId = savedUser.id,
                token = telegramToken
            )
            val savedToken = telegramTokenRepository.save(telegramTokeEntity)
            assertTrue(telegramTokenRepository.existsById(savedToken.id))

            val telegramToken = userService.getTelegramToken(savedUser.id)

            verify(exactly = 1) { userRepository.findById(savedUser.id) }
            verify(exactly = 1) { telegramTokenRepository.findByUserId(savedUser.id) }

            assertEquals(telegramToken.token, savedToken.token)
        }

        @Test
        fun `should throw an exception if user is not found`() {

            assertFalse(userRepository.existsById(userId))

            expectApiError(ApiError.USER_NOT_FOUND) {
                userService.getTelegramToken(userId)
            }
        }
    }

    @Nested
    inner class AuthByTelegramToken {

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))
            val telegramTokeEntity = TelegramTokenEntity(
                id = -1,
                userId = savedUser.id,
                token = telegramToken
            )
            val savedToken = telegramTokenRepository.save(telegramTokeEntity)
            assertTrue(telegramTokenRepository.existsById(savedToken.id))

            val telegramToken = userService.authByTelegramToken(SimpleTelegramTokenDto(savedToken.token))

            verify(exactly = 1) { telegramTokenRepository.findByToken(savedToken.token) }

            assertEquals(savedToken.token, telegramToken.token)
            assertEquals(savedUser.id, telegramToken.userId)
        }

        @Test
        fun `should throw an exception if token is not found`() {

            expectApiError(ApiError.TELEGRAM_TOKEN_NOT_FOUND) {
                userService.authByTelegramToken(simpleTelegramTokenDto)
            }
        }
    }
}

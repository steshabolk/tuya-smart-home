package ru.handh.project.service

import com.nimbusds.jwt.JWTClaimsSet
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.dto.TokenDto
import ru.handh.project.entity.RefreshTokenEntity
import ru.handh.project.entity.UserEntity
import ru.handh.project.enum.Signature
import ru.handh.project.exception.ApiError
import ru.handh.project.repository.RefreshTokenRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

private val log = KotlinLogging.logger {}

@Service
class TokenService(
    @Value("\${jwt.access.ttl}")
    private val accessTtl: Int,

    @Value("\${jwt.refresh.ttl}")
    private val refreshTtl: Int,

    private val refreshTokenRepository: RefreshTokenRepository,
    private val encryptionService: EncryptionService,
    private val signatureService: SignatureService

) {

    @Transactional
    fun getToken(user: UserEntity, encryption: Boolean, signature: Signature) =
        UUID.randomUUID().toString()
            .let {
                TokenDto(
                    accessToken = generateToken(
                        buildJWTClaims(it, user.id, accessTtl),
                        encryption,
                        signature
                    ),
                    refreshToken = it,
                    ttl = accessTtl
                )
            }.also {
                saveToken(it.refreshToken, refreshTtl)
            }

    @Transactional(readOnly = true)
    fun refresh(
        userId: Int,
        jwtId: String,
        encryption: Boolean,
        signature: Signature
    ): TokenDto {
        val refreshToken = refreshTokenRepository.findByJwtID(jwtId)
            ?: throw ApiError.REFRESH_TOKEN_NOT_FOUND.toException(jwtId)
        log.debug { "refresh access token by refresh token{id=${refreshToken.id}}" }
        return TokenDto(
            accessToken = generateToken(
                buildJWTClaims(refreshToken.jwtID, userId, accessTtl),
                encryption,
                signature
            ),
            refreshToken = refreshToken.jwtID,
            ttl = accessTtl
        )
    }

    @Transactional
    fun signOut(jwtId: String) =
        refreshTokenRepository.deleteByJwtID(jwtId)
            .also { log.debug { "refresh token{uuid=${jwtId}} was deleted" } }

    @Transactional
    fun removeExpiredTokens() =
        refreshTokenRepository.deleteAllByExpiresAtLessThanEqual(Instant.now())
            .also {
                log.debug { "remove expired tokens task executed" }
            }

    private fun saveToken(uuid: String, ttl: Int) =
        refreshTokenRepository.save(
            RefreshTokenEntity(
                jwtID = uuid,
                expiresAt = Instant.now().plus(ttl.toLong(), ChronoUnit.MINUTES)
            )
        ).also {
            log.debug { "generate new tokens pair={id=${it.id}}" }
        }

    private fun generateToken(jwtClaimsSet: JWTClaimsSet, encryption: Boolean, signature: Signature) =
        signatureService.sign(signature, jwtClaimsSet)
            .run {
                if (encryption) encryptionService.encrypt(this)
                else this.serialize()
            }

    private fun buildJWTClaims(uuid: String, userId: Int, ttl: Int) =
        JWTClaimsSet.Builder()
            .subject(userId.toString())
            .expirationTime(Date.from(Instant.now().plus(ttl.toLong(), ChronoUnit.MINUTES)))
            .jwtID(uuid)
            .build()
}

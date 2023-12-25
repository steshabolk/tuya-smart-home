package ru.handh.project.service

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.entity.RefreshTokenEntity
import ru.handh.project.enum.Signature
import ru.handh.project.exception.ApiError
import ru.handh.project.repository.RefreshTokenRepository
import ru.handh.project.repository.UserRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.JWKServiceMock
import ru.handh.project.util.expectApiError
import ru.handh.project.util.refreshToken
import ru.handh.project.util.tokenEntity
import ru.handh.project.util.userEntity
import ru.handh.project.util.userId
import java.time.Instant
import java.time.temporal.ChronoUnit

@Import(
    TokenService::class,
    JWKServiceMock::class,
    EncryptionService::class,
    SignatureService::class
)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class TokenServiceTest {

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @MockkBean
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @MockkBean
    private lateinit var userService: UserService

    @SpykBean
    private lateinit var userRepository: UserRepository

    private val jwtClaimsSetSlot = slot<JWTClaimsSet>()
    private val jwtSlot = slot<JWT>()
    private val refreshTokenSlot = slot<RefreshTokenEntity>()

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=NON"])
    inner class GetTokenWithoutEncryptionAndSignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { signatureService.sign(Signature.NON, capture(jwtClaimsSetSlot)) } answers { callOriginal() }
            every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers { callOriginal() }

            val tokenDto =
                tokenService.getToken(userEntity, false, Signature.NON)

            assertEquals(tokenDto.refreshToken, refreshTokenSlot.captured.jwtID)

            assertTrue(refreshTokenRepository.existsById(refreshTokenSlot.captured.id))
            verify(exactly = 1) { refreshTokenRepository.save(refreshTokenSlot.captured) }

            assertEquals(savedUser.id, jwtClaimsSetSlot.captured.subject.toInt())
            assertEquals(refreshTokenSlot.captured.jwtID, jwtClaimsSetSlot.captured.jwtid)
            verify(exactly = 1) { signatureService.sign(Signature.NON, capture(jwtClaimsSetSlot)) }

            verify(exactly = 0) { encryptionService.encrypt(any()) }

            assertFalse(jwkService.isSignKeysExist())
            assertFalse(jwkService.isEncryptionKeysExist())
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=true", "rsa.signature=NON"])
    inner class GetTokenWithEncryption {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { signatureService.sign(Signature.NON, capture(jwtClaimsSetSlot)) } answers { callOriginal() }
            every { encryptionService.encrypt(capture(jwtSlot)) } answers { callOriginal() }
            every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers { callOriginal() }

            val tokenDto =
                tokenService.getToken(userEntity, true, Signature.NON)

            assertEquals(tokenDto.refreshToken, refreshTokenSlot.captured.jwtID)

            assertTrue(refreshTokenRepository.existsById(refreshTokenSlot.captured.id))
            verify(exactly = 1) { refreshTokenRepository.save(refreshTokenSlot.captured) }

            assertEquals(savedUser.id, jwtClaimsSetSlot.captured.subject.toInt())
            assertEquals(refreshTokenSlot.captured.jwtID, jwtClaimsSetSlot.captured.jwtid)
            verify(exactly = 1) { signatureService.sign(Signature.NON, capture(jwtClaimsSetSlot)) }

            assertEquals(jwtClaimsSetSlot.captured, jwtSlot.captured.jwtClaimsSet)
            assertEquals(Algorithm.NONE, jwtSlot.captured.header.algorithm)
            verify(exactly = 1) { encryptionService.encrypt(jwtSlot.captured) }

            assertFalse(jwkService.isSignKeysExist())
            assertTrue(jwkService.isEncryptionKeysExist())

        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=true", "rsa.signature=SECRET"])
    inner class GetTokenWithEncryptionAndSecretSignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { signatureService.sign(Signature.SECRET, capture(jwtClaimsSetSlot)) } answers { callOriginal() }
            every { encryptionService.encrypt(capture(jwtSlot)) } answers { callOriginal() }
            every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers { callOriginal() }

            val tokenDto =
                tokenService.getToken(userEntity, true, Signature.SECRET)

            assertEquals(tokenDto.refreshToken, refreshTokenSlot.captured.jwtID)

            assertTrue(refreshTokenRepository.existsById(refreshTokenSlot.captured.id))
            verify(exactly = 1) { refreshTokenRepository.save(refreshTokenSlot.captured) }

            assertEquals(savedUser.id, jwtClaimsSetSlot.captured.subject.toInt())
            assertEquals(refreshTokenSlot.captured.jwtID, jwtClaimsSetSlot.captured.jwtid)
            verify(exactly = 1) { signatureService.sign(Signature.SECRET, capture(jwtClaimsSetSlot)) }

            assertEquals(jwtClaimsSetSlot.captured, jwtSlot.captured.jwtClaimsSet)
            assertEquals(JWSAlgorithm.HS256, jwtSlot.captured.header.algorithm)
            verify(exactly = 1) { encryptionService.encrypt(jwtSlot.captured) }

            assertFalse(jwkService.isSignKeysExist())
            assertTrue(jwkService.isEncryptionKeysExist())
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=true", "rsa.signature=RSA"])
    inner class GetTokenWithEncryptionAndRsaSignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedUser = userRepository.save(userEntity)
            assertTrue(userRepository.existsById(savedUser.id))

            every { signatureService.sign(Signature.RSA, capture(jwtClaimsSetSlot)) } answers { callOriginal() }
            every { encryptionService.encrypt(capture(jwtSlot)) } answers { callOriginal() }
            every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers { callOriginal() }

            val tokenDto =
                tokenService.getToken(userEntity, true, Signature.RSA)

            assertEquals(tokenDto.refreshToken, refreshTokenSlot.captured.jwtID)

            assertTrue(refreshTokenRepository.existsById(refreshTokenSlot.captured.id))
            verify(exactly = 1) { refreshTokenRepository.save(refreshTokenSlot.captured) }

            assertEquals(savedUser.id, jwtClaimsSetSlot.captured.subject.toInt())
            assertEquals(refreshTokenSlot.captured.jwtID, jwtClaimsSetSlot.captured.jwtid)
            verify(exactly = 1) { signatureService.sign(Signature.RSA, capture(jwtClaimsSetSlot)) }

            assertEquals(jwtClaimsSetSlot.captured, jwtSlot.captured.jwtClaimsSet)
            assertEquals(JWSAlgorithm.RS256, jwtSlot.captured.header.algorithm)
            verify(exactly = 1) { encryptionService.encrypt(jwtSlot.captured) }

            assertTrue(jwkService.isSignKeysExist())
            assertTrue(jwkService.isEncryptionKeysExist())
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=SECRET"])
    inner class RefreshToken {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedToken = refreshTokenRepository.save(tokenEntity)
            assertTrue(refreshTokenRepository.existsById(savedToken.id))

            val tokenDto = tokenService.refresh(userId, refreshToken, false, Signature.SECRET)

            assertEquals(savedToken.jwtID, tokenDto.refreshToken)
        }

        @Test
        fun `should throw an exception if refresh token is not found`() {

            assertEquals(null, refreshTokenRepository.findByJwtID(refreshToken))

            expectApiError(ApiError.REFRESH_TOKEN_NOT_FOUND) {
                tokenService.refresh(userId, refreshToken, false, Signature.SECRET)
            }
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=SECRET"])
    inner class SignOut {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val savedToken = refreshTokenRepository.save(tokenEntity)
            assertTrue(refreshTokenRepository.existsById(savedToken.id))

            tokenService.signOut(savedToken.jwtID)

            verify(exactly = 1) { refreshTokenRepository.deleteByJwtID(savedToken.jwtID) }
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=SECRET"])
    inner class RemoveExpiredTokens {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @SpykBean
        private lateinit var refreshTokenRepository: RefreshTokenRepository

        @Test
        fun success() {

            val expiredTokenEntity = RefreshTokenEntity(
                id = -1,
                jwtID = "a6b70359-cc45-42cd-b55e-872b527ef18c",
                expiresAt = Instant.now().minus(5, ChronoUnit.HOURS)
            )

            val savedToken = refreshTokenRepository.save(tokenEntity)
            val savedExpiredToken = refreshTokenRepository.save(expiredTokenEntity)
            assertEquals(2, refreshTokenRepository.findAll().size)

            tokenService.removeExpiredTokens()

            verify(exactly = 1) { refreshTokenRepository.deleteAllByExpiresAtLessThanEqual(any()) }

            assertTrue(refreshTokenRepository.existsById(savedToken.id))
            assertFalse(refreshTokenRepository.existsById(savedExpiredToken.id))
        }
    }
}

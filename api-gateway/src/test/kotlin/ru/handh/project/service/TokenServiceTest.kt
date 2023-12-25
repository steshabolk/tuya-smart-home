package ru.handh.project.service

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JWSAlgorithm
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import ru.handh.project.enum.RsaType
import ru.handh.project.enum.Signature
import ru.handh.project.exception.ApiError
import ru.handh.project.util.JWKServiceMock
import ru.handh.project.util.accessTtl
import ru.handh.project.util.expectApiError
import ru.handh.project.util.invalidAccessTokenToDecode
import ru.handh.project.util.invalidAccessTokenToParse
import ru.handh.project.util.refreshToken
import ru.handh.project.util.userId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@SpringBootTest(classes = [TokenService::class, JWKServiceMock::class, EncryptionService::class, SignatureService::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TokenServiceTest {

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=NON"])
    inner class TokenServiceWithoutEncryptionAndSignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @Test
        fun `token service available`() {

            val availability = tokenService.checkTokenServiceAvailability()

            assertTrue(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 0) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 0) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `decode token`() {

            val generatedToken = jwkService.generateToken(false, Signature.NON)
            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            val decodedToken = tokenService.getDecodedToken(generatedToken)

            assertEquals(userId, decodedToken.jwtClaimsSet.subject.toInt())
            assertEquals(refreshToken, decodedToken.jwtClaimsSet.jwtid)
            assertEquals(Algorithm.NONE, decodedToken.header.algorithm)

            verify(exactly = 0) { encryptionService.decrypt(any()) }
            verify(exactly = 0) { signatureService.verify(Signature.NON, any()) }
        }

        @Test
        fun `check expiration time of non-expired token`() {

            val token =
                jwkService.generateDecodedToken(Date.from(Instant.now().plus(accessTtl.toLong(), ChronoUnit.MINUTES)))

            val isExpired = tokenService.isExpired(token)

            assertFalse(isExpired)
        }

        @Test
        fun `check expiration time of expired token`() {

            val token =
                jwkService.generateDecodedToken(Date.from(Instant.now().minus(accessTtl.toLong(), ChronoUnit.MINUTES)))

            val isExpired = tokenService.isExpired(token)

            assertTrue(isExpired)
        }

        @Test
        fun `get user id from payload`() {

            val token =
                jwkService.generateDecodedToken(Date.from(Instant.now().plus(accessTtl.toLong(), ChronoUnit.MINUTES)))

            val userFromPayload = tokenService.getUserIdFromPayload(token)

            assertEquals(userId, userFromPayload)
        }

        @Test
        fun `get jwt id from payload`() {

            val token =
                jwkService.generateDecodedToken(Date.from(Instant.now().plus(accessTtl.toLong(), ChronoUnit.MINUTES)))

            val jwtIdFromPayload = tokenService.getJwtIdFromPayload(token)

            assertEquals(refreshToken, jwtIdFromPayload)
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=true", "rsa.signature=NON"])
    inner class TokenServiceWithEncryption {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @Test
        fun `token service available`() {

            val availability = tokenService.checkTokenServiceAvailability()

            assertTrue(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 1) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 0) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `encryption service is not available`() {

            every { jwkService.getKey(RsaType.ENCRYPT) } answers { null }

            val availability = tokenService.checkTokenServiceAvailability()

            assertFalse(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 1) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 0) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `decode token`() {

            val generatedToken = jwkService.generateToken(true, Signature.NON)
            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            val decodedToken = tokenService.getDecodedToken(generatedToken)

            assertEquals(userId, decodedToken.jwtClaimsSet.subject.toInt())
            assertEquals(refreshToken, decodedToken.jwtClaimsSet.jwtid)
            assertEquals(Algorithm.NONE, decodedToken.header.algorithm)

            verify(exactly = 1) { encryptionService.decrypt(any()) }
            verify(exactly = 0) { signatureService.verify(Signature.NON, any()) }
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=SECRET"])
    inner class TokenServiceWithSecretSignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @Test
        fun `token service available`() {

            val availability = tokenService.checkTokenServiceAvailability()

            assertTrue(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 0) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 0) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `decode token`() {

            val generatedToken = jwkService.generateToken(false, Signature.SECRET)
            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            val decodedToken = tokenService.getDecodedToken(generatedToken)

            assertEquals(userId, decodedToken.jwtClaimsSet.subject.toInt())
            assertEquals(refreshToken, decodedToken.jwtClaimsSet.jwtid)
            assertEquals(JWSAlgorithm.HS256, decodedToken.header.algorithm)

            verify(exactly = 0) { encryptionService.decrypt(any()) }
            verify(exactly = 1) { signatureService.verify(Signature.SECRET, any()) }
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=false", "rsa.signature=RSA"])
    inner class TokenServiceWithRSASignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @Test
        fun `token service available`() {

            val availability = tokenService.checkTokenServiceAvailability()

            assertTrue(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 0) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 1) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `signature service is not available`() {

            every { jwkService.getKey(RsaType.SIGN) } answers { null }

            val availability = tokenService.checkTokenServiceAvailability()

            assertFalse(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 0) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 1) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `decode token`() {

            val generatedToken = jwkService.generateToken(false, Signature.RSA)
            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            val decodedToken = tokenService.getDecodedToken(generatedToken)

            assertEquals(userId, decodedToken.jwtClaimsSet.subject.toInt())
            assertEquals(refreshToken, decodedToken.jwtClaimsSet.jwtid)
            assertEquals(JWSAlgorithm.RS256, decodedToken.header.algorithm)

            verify(exactly = 0) { encryptionService.decrypt(any()) }
            verify(exactly = 1) { signatureService.verify(Signature.RSA, any()) }
        }
    }

    @Nested
    @TestPropertySource(properties = ["rsa.isEncrypted=true", "rsa.signature=RSA"])
    inner class TokenServiceWithEncryptionAndRSASignature {

        @SpykBean
        private lateinit var jwkService: JWKServiceMock

        @SpykBean
        private lateinit var encryptionService: EncryptionService

        @SpykBean
        private lateinit var signatureService: SignatureService

        @SpykBean
        private lateinit var tokenService: TokenService

        @Test
        fun `token service available`() {

            val availability = tokenService.checkTokenServiceAvailability()

            assertTrue(availability)
            verify(exactly = 1) { encryptionService.isServiceAvailable() }
            verify(exactly = 1) { signatureService.isServiceAvailable() }
            verify(exactly = 1) { jwkService.getKey(RsaType.ENCRYPT) }
            verify(exactly = 1) { jwkService.getKey(RsaType.SIGN) }
        }

        @Test
        fun `decode token`() {

            val generatedToken = jwkService.generateToken(true, Signature.RSA)
            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            val decodedToken = tokenService.getDecodedToken(generatedToken)

            assertEquals(userId, decodedToken.jwtClaimsSet.subject.toInt())
            assertEquals(refreshToken, decodedToken.jwtClaimsSet.jwtid)
            assertEquals(JWSAlgorithm.RS256, decodedToken.header.algorithm)

            verify(exactly = 1) { encryptionService.decrypt(any()) }
            verify(exactly = 1) { signatureService.verify(Signature.RSA, any()) }
        }

        @Test
        fun `should throw an exception if token cant be parsed`() {

            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            expectApiError(ApiError.INVALID_JWT_TOKEN) {
                tokenService.getDecodedToken(invalidAccessTokenToParse)
            }
        }

        @Test
        fun `should throw an exception if token cant be decoded`() {

            assertTrue(encryptionService.isServiceAvailable() && signatureService.isServiceAvailable())

            expectApiError(ApiError.INVALID_JWT_TOKEN) {
                tokenService.getDecodedToken(invalidAccessTokenToDecode)
            }
        }
    }
}

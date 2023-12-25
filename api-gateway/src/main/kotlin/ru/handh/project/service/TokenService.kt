package ru.handh.project.service

import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.PlainJWT
import com.nimbusds.jwt.SignedJWT
import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.handh.project.enum.Signature
import ru.handh.project.exception.ApiError
import java.time.Instant
import java.util.Date

private val log = KotlinLogging.logger {}

@Service
class TokenService(
    private val encryptionService: EncryptionService,
    private val signatureService: SignatureService

) {

    fun checkTokenServiceAvailability(): Boolean {
        var availability = true
        if (!encryptionService.isServiceAvailable()) {
            availability = false
        }
        if (!signatureService.isServiceAvailable()) {
            availability = false
        }
        return availability
    }

    fun getDecodedToken(token: String) =
        decodeToken(token, encryptionService.isEncrypted, signatureService.signatureType)

    fun isExpired(decoded: JWT) =
        decoded.jwtClaimsSet.expirationTime <= Date.from(Instant.now())

    fun getUserIdFromPayload(decoded: JWT) =
        decoded.jwtClaimsSet?.subject?.toInt()

    fun getJwtIdFromPayload(decoded: JWT) =
        decoded.jwtClaimsSet?.jwtid

    private fun decodeToken(token: String, encryption: Boolean, signature: Signature): JWT {
        var jwt: JWT
        val isDecodedSuccessful: Boolean
        try {
            jwt = JWTParser.parse(token)
            if (encryption) {
                encryptionService.decrypt(jwt as EncryptedJWT)
                jwt =
                    if (signature != Signature.NON)
                        jwt.payload.toSignedJWT()
                    else PlainJWT(jwt.jwtClaimsSet)
            }
            isDecodedSuccessful =
                if (jwt is PlainJWT) true
                else signatureService.verify(signature, jwt as SignedJWT)
        } catch (ex: Exception) {
            throw ApiError.INVALID_JWT_TOKEN.toException()
        }
        if (!isDecodedSuccessful)
            throw ApiError.INVALID_JWT_TOKEN.toException()
        return jwt
    }
}

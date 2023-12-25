package ru.handh.project.service

import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.enum.RsaType
import ru.handh.project.enum.Signature
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey

@Component
class SignatureService(
    @Value("\${jwt.secret:}")
    private val secret: String? = null,

    @Value("\${rsa.signature}")
    private val signature: String,

    private val jwkService: JWKService
) {

    val signatureType = getSignatureType(signature)
    private var rsaKey: PublicKey? = null
    private var verifierRsa: RSASSAVerifier? = null
    private var verifierSecret: MACVerifier? = null

    fun isServiceAvailable(): Boolean {
        var availability = true
        if (signatureType == Signature.SECRET && verifierSecret == null) {
            verifierSecret = MACVerifier(secret)
        }
        if (signatureType == Signature.RSA && rsaKey == null) {
            jwkService.getKey(RsaType.SIGN)
                ?.run {
                    rsaKey = this as PublicKey
                    verifierRsa = RSASSAVerifier(rsaKey as RSAPublicKey)
                }
                ?: run { availability = false }
        }
        return availability
    }

    fun verify(signature: Signature, signedJWT: SignedJWT) =
        when (signature) {
            Signature.SECRET -> verifySignedWithSecret(signedJWT)
            Signature.RSA -> verifySignedWithRSA(signedJWT)
            Signature.NON -> true
        }

    private fun verifySignedWithSecret(signedJWT: SignedJWT) =
        signedJWT.verify(verifierSecret)

    private fun verifySignedWithRSA(signedJWT: SignedJWT) =
        signedJWT.verify(verifierRsa)

    private fun getSignatureType(prop: String) =
        when (prop) {
            Signature.SECRET.name -> Signature.SECRET
            Signature.RSA.name -> Signature.RSA
            else -> Signature.NON
        }
}

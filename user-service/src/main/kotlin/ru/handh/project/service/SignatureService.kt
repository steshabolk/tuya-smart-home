package ru.handh.project.service

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.enum.RsaType
import ru.handh.project.enum.Signature
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

@Component
class SignatureService(
    private val jwkService: JWKService,

    @Value("\${jwt.secret:}")
    private val secret: String? = null,

    @Value("\${rsa.signature}")
    private val signature: String,
) {

    final val signatureType = getSignatureType(signature)
    private var rsaKey: KeyPair? = null
    private var signerRsa: RSASSASigner? = null
    private var verifierRsa: RSASSAVerifier? = null
    private var signerSecret: MACSigner? = null
    private var verifierSecret: MACVerifier? = null

    init {
        if (signatureType == Signature.RSA) {
            jwkService.getKeys(RsaType.SIGN)
                .run {
                    rsaKey = this
                    signerRsa = RSASSASigner(rsaKey!!.private)
                    verifierRsa = RSASSAVerifier(rsaKey!!.public as RSAPublicKey)
                }
        }
        if (signatureType == Signature.SECRET) {
            signerSecret = MACSigner(secret)
            verifierSecret = MACVerifier(secret)
        }
    }

    fun sign(signature: Signature, jwtClaimsSet: JWTClaimsSet): JWT =
        when (signature) {
            Signature.SECRET -> signWithSecret(jwtClaimsSet)
            Signature.RSA -> signWithRSA(jwtClaimsSet)
            Signature.NON -> nonSigned(jwtClaimsSet)
        }

    fun verify(signature: Signature, signedJWT: SignedJWT) =
        when (signature) {
            Signature.SECRET -> verifySignedWithSecret(signedJWT)
            Signature.RSA -> verifySignedWithRSA(signedJWT)
            Signature.NON -> true
        }

    private fun signWithSecret(jwtClaimsSet: JWTClaimsSet) =
        SignedJWT(JWSHeader(Signature.SECRET.algorithm), jwtClaimsSet)
            .also { it.sign(signerSecret) }

    private fun verifySignedWithSecret(signedJWT: SignedJWT) =
        signedJWT.verify(verifierSecret)

    private fun signWithRSA(jwtClaimsSet: JWTClaimsSet) =
        SignedJWT(JWSHeader(Signature.RSA.algorithm), jwtClaimsSet)
            .also { it.sign(signerRsa) }

    private fun verifySignedWithRSA(signedJWT: SignedJWT) =
        signedJWT.verify(verifierRsa)

    private fun nonSigned(jwtClaimsSet: JWTClaimsSet) =
        PlainJWT(jwtClaimsSet)

    private fun getSignatureType(prop: String) =
        when (prop) {
            Signature.SECRET.name -> Signature.SECRET
            Signature.RSA.name -> Signature.RSA
            else -> Signature.NON
        }
}

package ru.handh.project.service

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.enum.RsaType
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

@Component
class EncryptionService(
    @Value("\${rsa.isEncrypted}")
    private val isEncrypted: Boolean,

    private val jwkService: JWKService
) {

    private val algorithm = JWEAlgorithm.RSA_OAEP_256
    private val method = EncryptionMethod.A128GCM
    private var rsaKey: KeyPair? = null
    private var encrypter: RSAEncrypter? = null
    private var decrypter: RSADecrypter? = null

    init {
        if (isEncrypted) {
            jwkService.getKeys(RsaType.ENCRYPT)
                .run {
                    rsaKey = this
                    encrypter = RSAEncrypter(rsaKey!!.public as RSAPublicKey)
                    decrypter = RSADecrypter(rsaKey!!.private)
                }
        }
    }

    fun encrypt(jwt: JWT): String =
        if (jwt is SignedJWT) {
            JWEObject(
                JWEHeader
                    .Builder(algorithm, method)
                    .contentType("JWT")
                    .build(),
                Payload(jwt)
            )
                .also { it.encrypt(encrypter) }
                .serialize()
        } else {
            EncryptedJWT(JWEHeader(algorithm, method), jwt.jwtClaimsSet)
                .also { it.encrypt(encrypter) }
                .serialize()
        }

    fun decrypt(encryptedJWT: EncryptedJWT) =
        encryptedJWT.decrypt(decrypter)
}

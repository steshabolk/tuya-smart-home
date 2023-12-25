package ru.handh.project.service

import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jwt.EncryptedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.enum.RsaType
import java.security.PrivateKey

@Component
class EncryptionService(
    @Value("\${rsa.isEncrypted}")
    val isEncrypted: Boolean,

    private val jwkService: JWKService
) {

    private var rsaKey: PrivateKey? = null
    private var decrypter: RSADecrypter? = null

    fun isServiceAvailable(): Boolean {
        var availability = true
        if (isEncrypted && rsaKey == null) {
            jwkService.getKey(RsaType.ENCRYPT)
                ?.run {
                    rsaKey = this as PrivateKey
                    decrypter = RSADecrypter(rsaKey)
                }
                ?: run { availability = false }
        }
        return availability
    }

    fun decrypt(encryptedJWT: EncryptedJWT) =
        encryptedJWT.decrypt(decrypter)
}

package ru.handh.project.service

import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.enum.KeyType
import ru.handh.project.enum.RsaType
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class JWKService(
    @Value("\${rsa.dir}")
    private val keysDir: String,

    @Value("\${rsa.sign.pub}")
    private val signPubFile: String,
    @Value("\${rsa.sign.private}")
    private val signPrivateFile: String,

    @Value("\${rsa.encrypt.pub}")
    private val encryptPubFile: String,
    @Value("\${rsa.encrypt.private}")
    private val encryptPrivateFile: String,
) {

    private val keyFactory = KeyFactory.getInstance("RSA")

    fun getKeys(rsaType: RsaType) =
        when (rsaType) {
            RsaType.SIGN -> getKeyPair(signPubFile, signPrivateFile, rsaType.keyUse)
            RsaType.ENCRYPT -> getKeyPair(encryptPubFile, encryptPrivateFile, rsaType.keyUse)
        }

    fun getKeyByType(keyType: KeyType, rsaType: RsaType): String? {
        val keyFile = when {
            keyType == KeyType.PUBLIC && rsaType == RsaType.SIGN -> signPubFile
            keyType == KeyType.PRIVATE && rsaType == RsaType.ENCRYPT -> encryptPrivateFile
            else -> null
        }
        log.debug { "load key for service request: keyType=$keyType, rsaType=$rsaType" }
        return keyFile
            ?.let { readFromFile(it) }
            ?.let { String(it) }
    }


    private fun getKeyPair(rsaPubFile: String, rsaPrivateFile: String, keyUse: KeyUse) =
        Pair(readFromFile(rsaPubFile), readFromFile(rsaPrivateFile))
            .run {
                let2(this.first, this.second) { pub, private ->
                    KeyPair(parsePubKey(pub, rsaPubFile), parsePrivateKey(private, rsaPrivateFile))
                }
                    ?: generateRSAKey(keyUse)
                        .also {
                            makeKeysDir()
                            writeToFile(rsaPubFile, it.public.encoded, KeyType.PUBLIC)
                            writeToFile(rsaPrivateFile, it.private.encoded, KeyType.PRIVATE)
                        }
            }

    private fun generateRSAKey(
        keyUse: KeyUse?,
        keyId: String = UUID.randomUUID().toString(),
        keySize: Int = 2048
    ) =
        RSAKeyGenerator(keySize)
            .keyID(keyId)
            .keyUse(keyUse)
            .generate()
            .toKeyPair()
            .also { log.debug { "generate new key pair: $keyUse" } }

    private fun readFromFile(file: String) =
        getKeysDir().resolve(file)
            .run {
                if (this.exists()) this.readBytes()
                else null
            }

    private fun parsePubKey(bytes: ByteArray, fileName: String) =
        X509EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PUBLIC))
        )
            .run { keyFactory.generatePublic(this) }
            .also { log.debug { "parse public key from resources: $fileName" } }

    private fun parsePrivateKey(bytes: ByteArray, fileName: String) =
        PKCS8EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PRIVATE))
        )
            .run { keyFactory.generatePrivate(this) }
            .also { log.debug { "parse private key from resources: $fileName" } }

    private fun String.parseKeyReplacement(type: KeyType) =
        this.replace(begin(type), "").replace(end(type), "")
            .replace("\r", "").replace("\n", "")

    private fun writeToFile(file: String, content: ByteArray, type: KeyType, chunkSize: Int = 64) =
        getKeysDir().resolve(file)
            .also {
                if (!it.exists()) {
                    it.createNewFile()
                    log.debug { "create file for key: $file" }
                }
            }
            .run {
                this.bufferedWriter().use { writer ->
                    writer.write(begin(type))
                    val chunked = Base64.getEncoder().encodeToString(content).chunked(chunkSize)
                    for (i in chunked.indices) {
                        writer.write(chunked[i])
                        if (i < chunked.size - 1) writer.newLine()
                    }
                    writer.write(end(type))
                }
            }

    private fun begin(type: KeyType) =
        "-----BEGIN ${type.type.uppercase()} KEY-----\n"

    private fun end(type: KeyType) =
        "\n-----END ${type.type.uppercase()} KEY-----"

    private fun makeKeysDir() =
        getKeysDir()
            .also { log.debug { "rsa keys dir path: $it " } }
            .mkdirs()
            .also { log.debug { "dir created: $it" } }

    protected fun getKeysDir() =
        File(keysDir).absoluteFile

    private inline fun <T1 : Any, T2 : Any, R : Any> let2(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? =
        if (p1 != null && p2 != null) block(p1, p2) else null
}

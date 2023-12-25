package ru.handh.project.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.handh.project.enum.KeyType
import ru.handh.project.enum.RsaType
import java.io.File
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

private val log = KotlinLogging.logger {}

@Component
class JWKService(
    @Value("\${rsa.dir}")
    private val keysDir: String,

    @Value("\${rsa.sign.pub}")
    private val signPubFile: String,
    @Value("\${rsa.encrypt.private}")
    private val encryptPrivateFile: String,

    @Value(value = "\${api.user.url}")
    private val userServiceURL: String,
    @Value(value = "\${api.user.keyPath}")
    private val loadKeyPath: String,

    private val restTemplate: RestTemplate
) {

    private val keyFactory = KeyFactory.getInstance("RSA")

    fun getKey(rsaType: RsaType): Key? =
        when (rsaType) {
            RsaType.SIGN -> loadKey(signPubFile, KeyType.PUBLIC, rsaType)
            RsaType.ENCRYPT -> loadKey(encryptPrivateFile, KeyType.PRIVATE, rsaType)
        }

    private fun loadKey(rsaFile: String, keyType: KeyType, rsaType: RsaType): Key? {
        val key = readFromFile(rsaFile)
            ?: requestKeyFromUserService(keyType, rsaType)
                ?.run {
                    makeKeysDir()
                    writeToFile(rsaFile, this)
                    readFromFile(rsaFile)
                }
        return key
            ?.run {
                when (keyType) {
                    KeyType.PUBLIC -> parsePubKey(this, rsaType)
                    KeyType.PRIVATE -> parsePrivateKey(this, rsaType)
                }
            }
    }

    private fun requestKeyFromUserService(keyType: KeyType, rsaType: RsaType): String? {
        val body: String?
        try {
            val uriComponents = UriComponentsBuilder
                .fromUriString("$userServiceURL/$loadKeyPath")
                .queryParam("keyType", keyType)
                .queryParam("rsaType", rsaType)
                .build()
            val requestEntity = RequestEntity<Any>(HttpMethod.GET, uriComponents.toUri())
            body = restTemplate.exchange(requestEntity, String::class.java).body
        } catch (ex: Exception) {
            log.error { "error loading RSA key from user service: ${ex.message}" }
            return null
        }
        body
            ?.also { log.debug { "load key from user service: keyType=$keyType, rsaType=$rsaType" } }
            ?: log.error { "error loading RSA key from user service: empty response" }
        return body
    }

    private fun readFromFile(file: String) =
        getKeysDir().resolve(file)
            .run {
                if (this.exists()) this.readBytes()
                else null
            }

    private fun writeToFile(file: String, content: String) =
        getKeysDir().resolve(file)
            .also {
                if (!it.exists()) {
                    it.createNewFile()
                    log.debug { "create file for key: $file" }
                }
            }
            .run {
                this.bufferedWriter().use { writer -> writer.write(content) }
            }

    private fun parsePubKey(bytes: ByteArray, rsaType: RsaType) =
        X509EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PUBLIC))
        )
            .run { keyFactory.generatePublic(this) }
            .also { log.debug { "parse public key from resources: rsaType=$rsaType" } }

    private fun parsePrivateKey(bytes: ByteArray, rsaType: RsaType) =
        PKCS8EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PRIVATE))
        )
            .run { keyFactory.generatePrivate(this) }
            .also { log.debug { "parse private key from resources: rsaType=$rsaType" } }

    private fun String.parseKeyReplacement(type: KeyType) =
        this.replace(begin(type), "").replace(end(type), "")
            .replace("\r", "").replace("\n", "")

    private fun begin(type: KeyType) =
        "-----BEGIN ${type.type.uppercase()} KEY-----\n"

    private fun end(type: KeyType) =
        "\n-----END ${type.type.uppercase()} KEY-----"

    private fun makeKeysDir() =
        getKeysDir()
            .also { log.debug { "rsa keys dir path: $it " } }
            .mkdirs()
            .also { log.debug { "dir created: $it" } }

    private fun getKeysDir() =
        File(keysDir).absoluteFile
}

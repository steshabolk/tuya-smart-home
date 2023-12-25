package ru.handh.project.util

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.handh.project.service.JWKService
import java.io.File

private val log = KotlinLogging.logger {}

@Component
class JWKServiceMock(
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
) : JWKService(
    keysDir = keysDir,
    signPubFile = signPubFile,
    signPrivateFile = signPrivateFile,
    encryptPubFile = encryptPubFile,
    encryptPrivateFile = encryptPrivateFile
) {

    init {
        clearResources()
    }

    private fun getResourceDir() =
        javaClass.classLoader.getResource("")?.toURI()!!

    override fun getKeysDir() =
        File(getResourceDir()).resolve(keysDir)

    private final fun clearResources() =
        File(getResourceDir().resolve(keysDir)).deleteRecursively()
            .also {
                log.debug {
                    "clear test resources, keys dir exist: ${
                        File(getResourceDir().resolve(keysDir)).exists()
                    }"
                }
            }

    fun isSignKeysExist() =
        File(getResourceDir().resolve(keysDir)).resolve(signPubFile).exists() ||
                File(getResourceDir().resolve(keysDir)).resolve(signPrivateFile).exists()

    fun isEncryptionKeysExist() =
        File(getResourceDir().resolve(keysDir)).resolve(encryptPubFile).exists() ||
                File(getResourceDir().resolve(keysDir)).resolve(encryptPrivateFile).exists()
}

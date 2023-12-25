package ru.handh.project.util

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.MockkBean
import io.mockk.impl.recording.WasNotCalled.method
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import ru.handh.project.enum.KeyType
import ru.handh.project.enum.RsaType
import ru.handh.project.enum.Signature
import ru.handh.project.service.JWKService
import java.security.Key
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date
import kotlin.math.sign

@Component
class JWKServiceMock(
    @Value("\${rsa.dir}")
    private val keysDir: String,

    @Value("\${rsa.sign.pub}")
    private val signPubFile: String,
    @Value("\${rsa.encrypt.private}")
    private val encryptPrivateFile: String,

    @Value("\${jwt.secret}")
    private val secret: String,

    @Value(value = "\${api.user.url}")
    private val userServiceURL: String,
    @Value(value = "\${api.user.keyPath}")
    private val loadKeyPath: String,

    @MockkBean
    private val restTemplate: RestTemplate
) : JWKService(
    keysDir = keysDir,
    signPubFile = signPubFile,
    encryptPrivateFile = encryptPrivateFile,
    userServiceURL = userServiceURL,
    loadKeyPath = loadKeyPath,
    restTemplate = restTemplate
) {

    private val signPrivateFile = "signPrivate.pem"
    private val encryptPubFile = "encryptPub.pub"

    private val keyFactory = KeyFactory.getInstance("RSA")

    override fun getKey(rsaType: RsaType): Key? {
        val key = getKeyFromResources(rsaType)
        return when (rsaType) {
            RsaType.SIGN -> parsePubKey(key)
            RsaType.ENCRYPT -> parsePrivateKey(key)
        }
    }

    private fun getKeyFromResources(rsaType: RsaType): ByteArray {
        val fileName = when (rsaType) {
            RsaType.SIGN -> signPubFile
            RsaType.ENCRYPT -> encryptPrivateFile
        }
        return javaClass.classLoader.getResource(fileName)!!.readBytes()
    }

    private fun parsePubKey(bytes: ByteArray) =
        X509EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PUBLIC))
        )
            .run { keyFactory.generatePublic(this) }

    private fun parsePrivateKey(bytes: ByteArray) =
        PKCS8EncodedKeySpec(
            Base64.getDecoder()
                .decode(String(bytes).parseKeyReplacement(KeyType.PRIVATE))
        )
            .run { keyFactory.generatePrivate(this) }

    private fun String.parseKeyReplacement(type: KeyType) =
        this.replace(begin(type), "").replace(end(type), "")
            .replace("\r", "").replace("\n", "")

    private fun begin(type: KeyType) =
        "-----BEGIN ${type.type.uppercase()} KEY-----\n"

    private fun end(type: KeyType) =
        "\n-----END ${type.type.uppercase()} KEY-----"

    fun generateDecodedToken(expirationTime: Date) =
        PlainJWT(
            JWTClaimsSet.Builder()
                .subject(userId.toString())
                .expirationTime(expirationTime)
                .jwtID(refreshToken)
                .build()
        )

    fun generateToken(encryption: Boolean, signature: Signature) =
        sign(signature, buildJWTClaims())
            .run {
                if (encryption) encrypt(this)
                else this.serialize()
            }

    private fun buildJWTClaims() =
        JWTClaimsSet.Builder()
            .subject(userId.toString())
            .expirationTime(Date.from(Instant.now().plus(accessTtl.toLong(), ChronoUnit.MINUTES)))
            .jwtID(refreshToken)
            .build()

    private fun encrypt(jwt: JWT): String {
        val encrypter =
            RSAEncrypter(parsePubKey(javaClass.classLoader.getResource(encryptPubFile)!!.readBytes()) as RSAPublicKey)
        return if (jwt is SignedJWT) {
            JWEObject(
                JWEHeader
                    .Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)
                    .contentType("JWT")
                    .build(),
                Payload(jwt)
            )
                .also { it.encrypt(encrypter) }
                .serialize()
        } else {
            EncryptedJWT(JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM), jwt.jwtClaimsSet)
                .also { it.encrypt(encrypter) }
                .serialize()
        }
    }

    private fun sign(signature: Signature, jwtClaimsSet: JWTClaimsSet): JWT =
        when (signature) {
            Signature.SECRET -> signWithSecret(jwtClaimsSet)
            Signature.RSA -> signWithRSA(jwtClaimsSet)
            Signature.NON -> nonSigned(jwtClaimsSet)
        }

    private fun signWithSecret(jwtClaimsSet: JWTClaimsSet) =
        SignedJWT(JWSHeader(Signature.SECRET.algorithm), jwtClaimsSet)
            .also { it.sign(MACSigner(secret)) }

    private fun signWithRSA(jwtClaimsSet: JWTClaimsSet): SignedJWT {
        val signerRsa = RSASSASigner(parsePrivateKey(javaClass.classLoader.getResource(signPrivateFile)!!.readBytes()))
        return SignedJWT(JWSHeader(Signature.RSA.algorithm), jwtClaimsSet)
            .also { it.sign(signerRsa) }
    }

    private fun nonSigned(jwtClaimsSet: JWTClaimsSet) =
        PlainJWT(jwtClaimsSet)
}

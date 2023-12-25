package ru.handh.project.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.handh.project.dto.SimpleTelegramTokenDto
import ru.handh.project.dto.TelegramTokenDto
import ru.handh.project.dto.TokenDto
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.DeleteAccountRequest
import ru.handh.project.dto.request.RefreshRequest
import ru.handh.project.dto.request.SignOutRequest
import ru.handh.project.entity.OutboxMessageEntity
import ru.handh.project.entity.RefreshTokenEntity
import ru.handh.project.entity.UserEntity
import ru.handh.project.model.UserDeletedEvent
import java.time.Instant
import java.time.temporal.ChronoUnit

private val mapper = jacksonObjectMapper()

// user service
const val userRequestMapping = "/api"
const val userId = 1
const val userIdNotFound = 1000
const val usernameNotFound = "user-123"
const val userName = "name-1"
const val userUsername = "username-1"
const val userPassword = "Password0"
const val userPasswordEncoded = "\$2a\$10\$Aj/x1blg0l6YpwsfRc0Cge36YE.xh1v1AKQKmxaEeUU5Vu76umMhq"
const val userPasswordNotMatch = "Password1"

val userEntity = UserEntity(
    id = -1,
    name = userName,
    username = userUsername,
    password = userPasswordEncoded
)

val deleteRequest = DeleteAccountRequest(
    password = userPassword
)

// token
const val accessToken = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.hKVWTGdRQxF9faIoHjTxgns" +
        "DA8mK7ELqwv82wssEzt5b61s7JYp0ryHf5rA8wWWg5efIAffe6fHYXAm6DJM6EiIDNE-C7UDnIrSFxeStRlDcTrXe_SQfd1bmNDBsjDNePdd" +
        "Bybif9byfGOK_3g1I6QMn7JkqiJo_MyEVWutWdFt8H0d8MRcOoN_R73PvV6SvhNhlFAVoAI4qzw1PCUHRMAoGMgs1Z6Cp7R_m-Mk_5VhFYT-" +
        "BLMJ9iMCOn78gMpOI_e60653wXb0s26n-q9Sg6H4LlAmGjp_340FAPCWzcLjN0a1znbBv2ziN6C4VYPmArzWiMxDfDZsgqn1LAYeAOg." +
        "MWCxpK1uSng-WmUz.undTU1Are0Sx_gMLZmEUJxS2jIdJ3vXxsPEldTZqtq8SXIzeT3josIIYYndPcA2vYJlxaYBzYOh966-Nn2QrRAbP-" +
        "IA7W9IZx4WdqDdjFBeO1eAENamIZyv239RR8KDYb350mZ9RKtMdu3VvQb126tiTwrQcAWZv1uwcDjt1NYA93qquSrQGAGRaPXCbB9qeqo" +
        "AGcKpgPws9O44SbAjj-ifQiA.3ayUjMVLtqpv9IO0Awrv5A"
const val refreshToken = "a6b70359-cc45-42cd-b55e-872b527ef18b"
const val accessTtl = 15
const val refreshTtl = 10_080

val tokenEntity = RefreshTokenEntity(
    id = -1,
    jwtID = refreshToken,
    expiresAt = Instant.now().plus(refreshTtl.toLong(), ChronoUnit.MINUTES)
)

val tokenDto = TokenDto(
    accessToken = accessToken,
    refreshToken = refreshToken,
    ttl = accessTtl
)

val refreshRequest = RefreshRequest(
    jwtId = refreshToken
)
val signOutRequest = SignOutRequest(
    jwtId = refreshToken
)

// kafka
const val userDeletedTopic = "user-deleted"

// outbox message service
val userDeletedEvent = UserDeletedEvent(
    message = DeletedMessageDto(userId)
)
val userDeletedMessage = OutboxMessageEntity(
    id = -1,
    topic = userDeletedTopic,
    message = mapper.writeValueAsString(userDeletedEvent)
)

// telegram
const val telegramToken = "a6b70359-cc45-42cd-b55e-872b527ef18b"
val simpleTelegramTokenDto = SimpleTelegramTokenDto(telegramToken)
val telegramTokenDto = TelegramTokenDto(telegramToken, userId)

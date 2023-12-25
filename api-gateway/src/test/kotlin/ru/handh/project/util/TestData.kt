package ru.handh.project.util

import ru.handh.project.dto.RefreshTokenDto

const val userId = 1
const val tokenHeader = "X-Access-Token"
const val register = "/api/register"
const val auth = "/api/auth"
const val telegram = "/api/telegram"
const val refresh = "/api/refresh"
const val signout = "/api/signout"

// token
const val invalidAccessTokenToParse = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.hKVWTGdRQxF9faIoHjTxgns"
const val invalidAccessTokenToDecode = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.hKVWTGdRQxF9faIoHjTxgns" +
        "DA8mK7ELqwv82wssEzt5b61s7JYp0ryHf5rA8wWWg5efIAffe6fHYXAm6DJM6EiIDNE-C7UDnIrSFxeStRlDcTrXe_SQfd1bmNDBsjDNePdd" +
        "Bybif9byfGOK_3g1I6QMn7JkqiJo_MyEVWutWdFt8H0d8MRcOoN_R73PvV6SvhNhlFAVoAI4qzw1PCUHRMAoGMgs1Z6Cp7R_m-Mk_5VhFYT-" +
        "BLMJ9iMCOn78gMpOI_e60653wXb0s26n-q9Sg6H4LlAmGjp_340FAPCWzcLjN0a1znbBv2ziN6C4VYPmArzWiMxDfDZsgqn1LAYeAOg." +
        "MWCxpK1uSng-WmUz.undTU1Are0Sx_gMLZmEUJxS2jIdJ3vXxsPEldTZqtq8SXIzeT3josIIYYndPcA2vYJlxaYBzYOh966-Nn2QrRAbP-" +
        "IA7W9IZx4WdqDdjFBeO1eAENamIZyv239RR8KDYb350mZ9RKtMdu3VvQb126tiTwrQcAWZv1uwcDjt1NYA93qquSrQGAGRaPXCbB9qeqo" +
        "AGcKpgPws9O44SbAjj-ifQiA.3ayUjMVLtqpv9IO0Awrv5A"
const val accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzAxNjUzMzY2LCJqdGkiOiJhNmI3MDM1OS1jYzQ1LTQyY2QtYjU1ZS04NzJiNTI3ZWYxOGIifQ.rjewt5YSveAUDsvvJTZaeq30pbegOc42B15TiPRXVKI"
const val refreshToken = "a6b70359-cc45-42cd-b55e-872b527ef18b"
const val accessTtl = 15

val refreshTokenDto = RefreshTokenDto(
    refreshToken = refreshToken
)

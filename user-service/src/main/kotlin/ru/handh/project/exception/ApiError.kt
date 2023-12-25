package ru.handh.project.exception

import org.springframework.http.HttpStatus

enum class ApiError(
    private val status: HttpStatus,
    private val messageProp: String,
) {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.userNotFound"),
    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.usernameNotFound"),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "ex.api.wrongPassword"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.refreshTokenNotFound"),
    TOPIC_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "ex.api.topicNotFound"),
    TELEGRAM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.telegramTokenNotFound");

    fun toException(vararg args: Any) =
        ApiException(
            messageProp = messageProp,
            messageArgs = args.map { it.toString() }.toTypedArray(),
            code = name,
            status = status
        )
}

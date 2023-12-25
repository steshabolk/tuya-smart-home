package ru.handh.project.exception

import org.springframework.http.HttpStatus

enum class ApiError(
    private val status: HttpStatus,
    private val messageProp: String,
) {
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "ex.api.accessTokenExpired"),
    MISSING_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "ex.api.missingAccessToken"),
    INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "ex.api.invalidJwtToken"),
    MISSING_REQUEST_ATTRIBUTE(HttpStatus.BAD_REQUEST, "ex.api.missingRequestAttribute"),
    TOKENS_NOT_MATCH(HttpStatus.BAD_REQUEST, "ex.api.tokensNotMatch");

    fun toException(vararg args: Any) =
        ApiException(
            messageProp = messageProp,
            messageArgs = args.map { it.toString() }.toTypedArray(),
            code = name,
            status = status
        )
}

package ru.handh.project.exception

import org.springframework.http.HttpStatus

enum class ApiError(
    private val status: HttpStatus,
    private val messageProp: String,
) {
    HOME_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.homeNotFound"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.roomNotFound"),
    OWNER_NOT_MATCH(HttpStatus.BAD_REQUEST, "ex.api.ownerNotMatch"),
    TOPIC_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "ex.api.topicNotFound");

    fun toException(vararg args: Any) =
        ApiException(
            messageProp = messageProp,
            messageArgs = args.map { it.toString() }.toTypedArray(),
            code = name,
            status = status
        )
}

package ru.handh.project.exception

import org.springframework.http.HttpStatus

enum class ApiError(
    private val status: HttpStatus,
    private val messageProp: String,
) {
    UNKNOWN_DEVICE_CATEGORY(HttpStatus.BAD_REQUEST, "ex.api.unknownDeviceCategory"),
    UNKNOWN_COMMAND_CODE(HttpStatus.BAD_REQUEST, "ex.api.unknownCommandCode"),
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "ex.api.deviceNotFound"),
    OWNER_NOT_MATCH(HttpStatus.BAD_REQUEST, "ex.api.ownerNotMatch"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "ex.api.invalidInput");

    fun toException(vararg args: Any) =
        ApiException(
            messageProp = messageProp,
            messageArgs = args.map { it.toString() }.toTypedArray(),
            code = name,
            status = status
        )
}

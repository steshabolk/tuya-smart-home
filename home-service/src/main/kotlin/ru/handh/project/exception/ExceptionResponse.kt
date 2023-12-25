package ru.handh.project.exception

import java.time.LocalDateTime

data class ExceptionResponse(
    val code: String,
    val message : String,
    val timestamp : LocalDateTime
)

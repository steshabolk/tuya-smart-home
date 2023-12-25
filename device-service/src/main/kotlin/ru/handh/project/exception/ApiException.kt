package ru.handh.project.exception

import org.springframework.http.HttpStatus

class ApiException(
    val messageProp: String,
    val messageArgs: Array<String>,
    val code: String,
    val status: HttpStatus
) : Exception()

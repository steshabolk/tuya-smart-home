package ru.handh.project.exception

import org.springframework.http.HttpStatus

class ApiResponseStatus(
    val status: HttpStatus
) : Exception()

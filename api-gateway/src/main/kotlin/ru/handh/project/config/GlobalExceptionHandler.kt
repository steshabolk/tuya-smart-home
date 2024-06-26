package ru.handh.project.config

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ru.handh.project.exception.ApiException
import ru.handh.project.exception.ApiResponseStatus
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.service.GatewayService
import ru.handh.project.util.Messages
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler(
    private val messages: Messages,
    private val gatewayService: GatewayService
) {

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ExceptionResponse> {
        log.debug { ex.message }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ExceptionResponse(
                    code = "INTERNAL_API_ERROR",
                    message = ex.message.toString(),
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(ApiResponseStatus::class)
    fun handleApiResponseStatus(ex: ApiResponseStatus): ResponseEntity<Unit> {
        return ResponseEntity.status(ex.status).build()
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientException(ex: HttpClientErrorException): ResponseEntity<Any> {
        log.debug { ex.message }
        val servletPath =
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request.servletPath
        return ResponseEntity
            .status(ex.statusCode)
            .body(
                ex.getResponseBodyAs(
                    gatewayService.getClientTypeByServletPath(servletPath).errorClass
                )
            )
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ExceptionResponse> {
        val message = messages.getMessageFromProp(ex.messageProp, ex.messageArgs)
        log.debug { message }
        return ResponseEntity
            .status(ex.status)
            .body(
                ExceptionResponse(
                    code = ex.code,
                    message = message,
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val message = validationFieldsMessage(ex.bindingResult)
        log.debug { message }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ExceptionResponse(
                    code = "VALIDATION_ERROR",
                    message = message,
                    timestamp = LocalDateTime.now()
                )
            )
    }

    private fun validationFieldsMessage(errors: Errors) =
        errors.fieldErrors
            .joinToString(separator = "; ") { "${it.field}: ${it.defaultMessage}" }
}

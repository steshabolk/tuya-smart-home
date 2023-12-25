package ru.handh.project.config

import com.tuya.connector.api.exceptions.ConnectorException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ElementKind
import jakarta.validation.Path
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.Errors
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ru.handh.project.exception.ApiException
import ru.handh.project.exception.ApiResponseStatus
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.util.Messages
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler(
    private val messages: Messages
) {

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

    @ExceptionHandler(ApiResponseStatus::class)
    fun handleApiResponseStatus(ex: ApiResponseStatus): ResponseEntity<Unit> {
        return ResponseEntity.status(ex.status).build()
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

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ExceptionResponse> {
        val message = constraintViolationMessage(ex.constraintViolations)
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

    @ExceptionHandler(ConnectorException::class)
    fun handleConnectorException(ex: ConnectorException): ResponseEntity<ExceptionResponse> {
        log.debug { ex.message }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ExceptionResponse(
                    code = "TUYA_CONNECTOR_ERROR",
                    message = ex.message.toString(),
                    timestamp = LocalDateTime.now()
                )
            )
    }

    private fun validationFieldsMessage(errors: Errors) =
        errors.fieldErrors
            .joinToString(separator = "; ") { "${it.field}: ${it.defaultMessage}" }

    private fun constraintViolationMessage(errors: Set<ConstraintViolation<*>>) =
        errors
            .joinToString(separator = "; ") { "${getPropertyNode(it.propertyPath)}: ${it.message}" }

    private fun getPropertyNode(path: Path) =
        path.associateBy { it.kind }[ElementKind.PROPERTY] ?: path
}

package ru.handh.project.config

import mu.KotlinLogging
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.handh.project.sender.TelegramBotMessageSender

private val log = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender
) {

    @ExceptionHandler(TelegramApiException::class)
    fun handleTelegramApiException(ex: TelegramApiException) {
        log.debug { "telegram error: " + ex.message }
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception) {
        log.debug { "api error: " + ex.message }
    }
}

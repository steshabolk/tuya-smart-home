package ru.handh.project.util

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class Messages(
    private val messageSource: MessageSource
) {

    fun getMessageFromProp(messageProp: String, args: Array<String>) =
        messageSource.getMessage(messageProp, args, LocaleContextHolder.getLocale())

    fun getMessageFromProp(messageProp: String) =
        messageSource.getMessage(messageProp, emptyArray(), LocaleContextHolder.getLocale())

}

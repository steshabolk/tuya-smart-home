package ru.handh.project.sender

import com.vdurmont.emoji.EmojiParser
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultAbsSender
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.handh.project.property.TelegramBotProperties
import ru.handh.project.utils.errorMessage

@Component
class TelegramBotMessageSender(
    telegramBotProperties: TelegramBotProperties
) : DefaultAbsSender(DefaultBotOptions(), telegramBotProperties.token) {
    fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Int? = null,
        buttons: List<List<InlineKeyboardButton>>? = null,
        reply: ForceReplyKeyboard? = null
    ) = SendMessage.builder().apply {
        chatId(chatId)
        text(EmojiParser.parseToUnicode(text))
        replyToMessageId(replyToMessageId)
        buttons?.let {
            replyMarkup(
                InlineKeyboardMarkup.builder()
                    .keyboard(it)
                    .build()
            )
        }
        reply?.let {
            replyMarkup(reply)
        }
        parseMode(ParseMode.MARKDOWN)
    }.build().let { execute(it) }

    fun editMessage(
        chatId: Long,
        messageId: Int,
        text: String,
        buttons: List<List<InlineKeyboardButton>>? = null,
    ) = EditMessageText.builder().apply {
        chatId(chatId)
        messageId(messageId)
        text(EmojiParser.parseToUnicode(text))
        buttons?.let {
            replyMarkup(
                InlineKeyboardMarkup.builder()
                    .keyboard(it)
                    .build()
            )
        }
        parseMode(ParseMode.MARKDOWN)
    }.build().let { execute(it) }

    fun deleteMessage(chatId: Long, messageId: Int) =
        DeleteMessage.builder().apply {
            chatId(chatId)
            messageId(messageId)
        }.build().let { execute(it) }

    fun sendErrorMessage(
        chatId: Long,
    ) = SendMessage.builder().apply {
        chatId(chatId)
        text(EmojiParser.parseToUnicode(errorMessage()))
        parseMode(ParseMode.MARKDOWN)
    }.build().let { execute(it) }
}

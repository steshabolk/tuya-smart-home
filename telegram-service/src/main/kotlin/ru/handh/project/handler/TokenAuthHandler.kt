package ru.handh.project.handler

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.forceReply
import ru.handh.project.utils.invalidTokenMessage
import ru.handh.project.utils.isReply
import ru.handh.project.utils.loginMessage

@Component
class TokenAuthHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService,
    private val accountHandler: AccountHandler
) : ActionHandler {
    override val type: ActionType = ActionType.TOKEN_AUTH

    private val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex()

    override fun isTriggered(update: Update) =
        isReply(update.message?.text, loginMessage())

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chatId = update.message.chatId
        val token = update.message.text.trim()
        var isValid = uuidRegex.matches(token)
        if (isValid) {
            try {
                chatService.authByToken(token = token, chatId = chatId)
                    .run {
                        telegramBotMessageSender.deleteMessage(chatId = chatId, messageId = update.message.messageId)
                        accountHandler.handle(update, authChat = this)
                    }
            } catch (ex: Exception) {
                if (ex is HttpClientErrorException && ex.statusCode == HttpStatus.NOT_FOUND) isValid = false
                else telegramBotMessageSender.sendErrorMessage(chatId = chatId)
            }
        }
        if (!isValid) {
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = invalidTokenMessage(),
                reply = forceReply()
            )
        }
    }
}

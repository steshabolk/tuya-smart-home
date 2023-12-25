package ru.handh.project.handler

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import ru.handh.project.dto.action.AccountData
import ru.handh.project.dto.action.TokenAuthData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.service.ChatService.Companion.setReply
import ru.handh.project.utils.forceReply
import ru.handh.project.utils.invalidTokenMessage

@Component
class TokenAuthHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService,
    private val accountHandler: AccountHandler
) : ActionHandler<TokenAuthData> {
    override val type: ActionType = ActionType.TOKEN_AUTH

    private val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex()
    override fun handle(data: TokenAuthData, authChat: ChatEntity?) {
        var isValid = uuidRegex.matches(data.messageText)
        if (isValid) {
            try {
                chatService.authByToken(token = data.messageText, chatId = data.chatId)
                    .run {
                        telegramBotMessageSender.deleteMessage(chatId = data.chatId, messageId = data.messageId)
                        accountHandler.handle(
                            data = AccountData(chatId = data.chatId, messageId = data.messageId),
                            authChat = this
                        )
                    }
            } catch (ex: Exception) {
                if (ex is HttpClientErrorException && ex.statusCode == HttpStatus.NOT_FOUND) isValid = false
                else telegramBotMessageSender.sendErrorMessage(chatId = data.chatId)
            }
        }
        if (!isValid) {
            setReply(data.messageId, ActionType.TOKEN_AUTH)
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = invalidTokenMessage(),
                reply = forceReply()
            )
        }
    }
}

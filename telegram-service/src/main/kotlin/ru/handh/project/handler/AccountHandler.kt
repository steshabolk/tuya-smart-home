package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.LOGIN_BUTTON
import ru.handh.project.utils.LOGOUT_BUTTON
import ru.handh.project.utils.loggedInMessage
import ru.handh.project.utils.notLoggedInMessage

@Component
class AccountHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService
) : ActionHandler {
    override val type: ActionType = ActionType.ACCOUNT

    override fun isTriggered(update: Update) =
        update.message?.text == type.command

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chatId = update.message.chatId
        val chat = authChat?.let { true } ?: chatService.existsByChatId(chatId)
        if (chat) {
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = loggedInMessage(),
                buttons = listOf(
                    listOf(LOGOUT_BUTTON),
                )
            )
        } else {
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = notLoggedInMessage(),
                buttons = listOf(
                    listOf(LOGIN_BUTTON)
                )
            )
        }
    }
}

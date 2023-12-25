package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.AccountData
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
) : ActionHandler<AccountData> {
    override val type: ActionType = ActionType.ACCOUNT

    override fun handle(data: AccountData, authChat: ChatEntity?) {
        val chat = authChat?.let { true } ?: chatService.existsByChatId(data.chatId)
        if (chat) {
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = loggedInMessage(),
                buttons = listOf(
                    listOf(LOGOUT_BUTTON),
                )
            )
        } else {
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = notLoggedInMessage(),
                buttons = listOf(
                    listOf(LOGIN_BUTTON)
                )
            )
        }
    }
}

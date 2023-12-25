package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.LoginData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService.Companion.setReply
import ru.handh.project.utils.forceReply
import ru.handh.project.utils.loginMessage

@Component
class LoginHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender
) : ActionHandler<LoginData> {
    override val type: ActionType = ActionType.LOGIN

    override fun handle(data: LoginData, authChat: ChatEntity?) {
        setReply(data.messageId, ActionType.TOKEN_AUTH)
        telegramBotMessageSender.sendMessage(
            chatId = data.chatId,
            text = loginMessage(),
            reply = forceReply()
        )
    }
}

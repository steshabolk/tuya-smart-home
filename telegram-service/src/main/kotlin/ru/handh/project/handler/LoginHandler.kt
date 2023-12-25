package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.utils.forceReply
import ru.handh.project.utils.loginMessage

@Component
class LoginHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender
) : ActionHandler {
    override val type: ActionType = ActionType.LOGIN

    override fun isTriggered(update: Update) =
        update.message?.text == type.command

    override fun handle(update: Update, authChat: ChatEntity?) {
        telegramBotMessageSender.sendMessage(
            chatId = update.message.chatId,
            text = loginMessage(),
            reply = forceReply()
        )
    }
}

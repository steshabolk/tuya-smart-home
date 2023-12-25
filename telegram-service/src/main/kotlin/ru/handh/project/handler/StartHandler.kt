package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.utils.startMessage

@Component
class StartHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender
) : ActionHandler {
    override val type: ActionType = ActionType.START

    override fun isTriggered(update: Update) =
        update.message?.text == type.command

    override fun handle(update: Update, authChat: ChatEntity?) {
        telegramBotMessageSender.sendMessage(
            chatId = update.message.chatId,
            text = startMessage(update.message.chat.firstName)
        )
    }
}

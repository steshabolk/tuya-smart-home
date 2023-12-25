package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.StartData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.utils.startMessage

@Component
class StartHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender
) : ActionHandler<StartData> {
    override val type: ActionType = ActionType.START

    override fun handle(data: StartData, authChat: ChatEntity?) {
        telegramBotMessageSender.sendMessage(
            chatId = data.chatId,
            text = startMessage(data.firstName)
        )
    }
}

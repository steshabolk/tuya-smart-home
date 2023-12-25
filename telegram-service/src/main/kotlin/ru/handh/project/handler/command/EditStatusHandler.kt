package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import ru.handh.project.dto.command.EditStatusData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler
import ru.handh.project.handler.HandlerHelper
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.utils.forceReply

@Component
class EditStatusHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val handlerHelper: HandlerHelper
) : ActionHandler<EditStatusData> {
    override val type: ActionType = ActionType.EDIT_STATUS

    override fun handle(data: EditStatusData, authChat: ChatEntity?) {
        handlerHelper.getAuthChat(data.chatId, data.messageId) ?: return
        val deviceCommand = handlerHelper.getDeviceCommand(data.chatId) ?: return
        telegramBotMessageSender.sendMessage(
            chatId = data.chatId,
            text = deviceCommand.replyMessage(),
            reply = forceReply()
        )
    }
}

package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.DeviceData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.enum.DeviceCommand.Companion.commandsKeyboard
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.deviceStatusMessage

@Component
class DeviceHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService,
    private val handlerHelper: HandlerHelper
) : ActionHandler<DeviceData> {
    override val type: ActionType = ActionType.DEVICE

    override fun handle(data: DeviceData, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(data.chatId, data.messageId) ?: return
        val deviceId = handlerHelper.getDeviceId(data.chatId) ?: return
        try {
            val device = chatService.getDevice(chat.userId, deviceId)
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = deviceStatusMessage(device),
                buttons = commandsKeyboard(2)
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = data.chatId)
        }
    }
}

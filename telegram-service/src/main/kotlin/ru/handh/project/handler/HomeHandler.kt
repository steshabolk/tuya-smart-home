package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.HomeData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.chooseDeviceMessage
import ru.handh.project.utils.deviceButton
import ru.handh.project.utils.emptyDevicesMessage
import ru.handh.project.utils.getMultilevelKeyboard

@Component
class HomeHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService,
    private val handlerHelper: HandlerHelper
) : ActionHandler<HomeData> {
    override val type: ActionType = ActionType.HOME

    override fun handle(data: HomeData, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(data.chatId, data.messageId) ?: return
        val homeId = handlerHelper.getHomeId(data.chatId) ?: return
        try {
            val devices = chatService.getDevices(chat.userId, homeId)
            if (devices.isEmpty()) {
                telegramBotMessageSender.sendMessage(
                    chatId = data.chatId,
                    text = emptyDevicesMessage()
                )
                return
            }
            val keyboard = devices
                .mapIndexed { idx, device -> deviceButton(device.id, idx + 1) }
                .getMultilevelKeyboard(4)
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = chooseDeviceMessage(devices),
                buttons = keyboard
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = data.chatId)
        }
    }
}

package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
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
) : ActionHandler {
    override val type: ActionType = ActionType.DEVICE

    override fun isTriggered(update: Update) =
        update.callbackQuery?.data?.startsWith(type.command) ?: false

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(update) ?: return
        val deviceId = update.callbackQuery?.data?.replace(ActionType.DEVICE.command, "")?.toInt() ?: return
        val chatId = update.message.chatId
        try {
            val device = chatService.getDevice(chat.userId, deviceId)
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = deviceStatusMessage(device),
                buttons = commandsKeyboard(2, deviceId)
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = chatId)
        }
    }
}

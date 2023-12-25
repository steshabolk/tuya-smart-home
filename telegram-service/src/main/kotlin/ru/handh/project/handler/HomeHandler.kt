package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
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
) : ActionHandler {
    override val type: ActionType = ActionType.HOME

    override fun isTriggered(update: Update) =
        update.callbackQuery?.data?.startsWith(type.command) ?: false

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(update) ?: return
        val chatId = update.message.chatId
        val homeId = update.callbackQuery.data.replace(type.command, "").toInt()
        try {
            val devices = chatService.getDevices(chat.userId, homeId)
            if (devices.isEmpty()) {
                telegramBotMessageSender.sendMessage(
                    chatId = chatId,
                    text = emptyDevicesMessage()
                )
                return
            }
            val keyboard = devices
                .mapIndexed { idx, device -> deviceButton(device.id, idx + 1) }
                .getMultilevelKeyboard(4)
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = chooseDeviceMessage(devices),
                buttons = keyboard
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = chatId)
        }
    }
}

package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.AccountData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.service.ChatService.Companion.getDevice
import ru.handh.project.service.ChatService.Companion.getHome

@Component
class HandlerHelper(
    private val chatService: ChatService,
    private val accountHandler: AccountHandler,
    private val telegramBotMessageSender: TelegramBotMessageSender,
) {

    fun getAuthChat(chatId: Long, messageId: Int): ChatEntity? {
        val chat = chatService.findByChatId(chatId)
        if (chat == null) {
            accountHandler.handle(
                data = AccountData(chatId = chatId, messageId = messageId),
                authChat = null
            )
        }
        return chat
    }

    fun getDeviceId(chatId: Long): Int? {
        val device = getDevice()
        if (device == null) {
            telegramBotMessageSender.sendErrorMessage(chatId)
        }
        return device
    }

    fun getHomeId(chatId: Long): Int? {
        val home = getHome()
        if (home == null) {
            telegramBotMessageSender.sendErrorMessage(chatId)
        }
        return home
    }

    fun getDeviceCommand(chatId: Long): DeviceCommand? {
        val command = ChatService.getDeviceCommand()
        if (command == null) {
            telegramBotMessageSender.sendErrorMessage(chatId)
        }
        return command?.deviceCommand
    }
}

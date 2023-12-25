package ru.handh.project.handler.command

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import ru.handh.device.client.model.CommandDtoGen
import ru.handh.project.dto.action.DeviceData
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.enum.DeviceCommand.Companion.toCommandCodeGen
import ru.handh.project.handler.DeviceHandler
import ru.handh.project.handler.HandlerHelper
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.service.ChatService.Companion.setDeviceCommand
import ru.handh.project.utils.forceReply

@Component
class CommandHandlerHelper(
    private val handlerHelper: HandlerHelper,
    private val chatService: ChatService,
    private val deviceHandler: DeviceHandler,
    private val telegramBotMessageSender: TelegramBotMessageSender

) {

    fun handleDeviceCommand(chatId: Long, messageId: Int, messageText:String) {
        val chat = handlerHelper.getAuthChat(chatId, messageId) ?: return
        val deviceCommand = handlerHelper.getDeviceCommand(chatId) ?: return
        val value = deviceCommand.parseValue(messageText) ?: run {
            handleInvalidCommandValue(chatId, messageId, deviceCommand)
            return
        }
        val deviceId = handlerHelper.getDeviceId(chatId) ?: return
        try {
            chatService.editDeviceStatus(
                chat.userId,
                deviceId,
                CommandDtoGen(code = deviceCommand.toCommandCodeGen(), value = value)
            )
            deviceHandler.handle(DeviceData(chatId = chatId, messageId = messageId), chat)
        } catch (ex: Exception) {
            if (ex is HttpClientErrorException && (ex.statusCode == HttpStatus.BAD_REQUEST || ex.statusCode == HttpStatus.NOT_FOUND)) {
                handleInvalidCommandValue(chatId, messageId, deviceCommand)
            } else telegramBotMessageSender.sendErrorMessage(chatId = chatId)
        }
    }

    private fun handleInvalidCommandValue(chatId: Long, messageId: Int, deviceCommand: DeviceCommand) {
        setDeviceCommand(
            messageId = messageId,
            command = deviceCommand
        )
        telegramBotMessageSender.sendMessage(
            chatId = chatId,
            text = deviceCommand.invalidCommandValueMessage,
            reply = forceReply()
        )
    }
}

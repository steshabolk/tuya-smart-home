package ru.handh.project.handler.command

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.device.client.model.CommandDtoGen
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.enum.DeviceCommand.Companion.toCommandCodeGen
import ru.handh.project.handler.DeviceHandler
import ru.handh.project.handler.HandlerHelper
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.forceReply

@Component
class CommandHandlerHelper(
    private val handlerHelper: HandlerHelper,
    private val chatService: ChatService,
    private val deviceHandler: DeviceHandler,
    private val telegramBotMessageSender: TelegramBotMessageSender

) {

    fun handleDeviceCommand(update: Update, deviceCommand: DeviceCommand) {
        val chat = handlerHelper.getAuthChat(update) ?: return
        val chatId = update.message.chatId
        val value = deviceCommand.parseValue(update.message.text) ?: run {
            handleInvalidCommandValue(chatId, deviceCommand)
            return
        }
        val replyMessage = update.message.replyToMessage.text
        val deviceId = replyMessage.substring(
            replyMessage.indexOf("№") + 1,
            replyMessage.indexOf(" ")
        )
            .toInt()
        try {
            chatService.editDeviceStatus(
                chat.userId,
                deviceId,
                CommandDtoGen(code = deviceCommand.toCommandCodeGen(), value = value)
            )
            deviceHandler.handle(update, chat)
        } catch (ex: Exception) {
            if (ex is HttpClientErrorException && (ex.statusCode == HttpStatus.BAD_REQUEST || ex.statusCode == HttpStatus.NOT_FOUND)) {
                handleInvalidCommandValue(chatId, deviceCommand)
            } else telegramBotMessageSender.sendErrorMessage(chatId = chatId)
        }
    }

    private fun handleInvalidCommandValue(chatId: Long, deviceCommand: DeviceCommand) {
        telegramBotMessageSender.sendMessage(
            chatId = chatId,
            text = deviceCommand.invalidCommandValueMessage,
            reply = forceReply()
        )
    }
}

package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.handler.ActionHandler
import ru.handh.project.handler.HandlerHelper
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.utils.forceReply

@Component
class EditStatusHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val handlerHelper: HandlerHelper
) : ActionHandler {
    override val type: ActionType = ActionType.EDIT_STATUS

    override fun isTriggered(update: Update) =
        DeviceCommand.deviceCommands
            .any { update.callbackQuery?.data?.startsWith(it) ?: false }

    override fun handle(update: Update, authChat: ChatEntity?) {
        handlerHelper.getAuthChat(update) ?: return
        val deviceCallback = update.callbackQuery?.data ?: return
        val deviceCommand = deviceCallback.let { DeviceCommand.from(it) } ?: return
        val chatId = update.message.chatId
        telegramBotMessageSender.sendMessage(
            chatId = chatId,
            text = "№${deviceCallback.replace(deviceCommand.actionType.command, "")} ${deviceCommand.replyMessage()}",
            reply = forceReply()
        )
    }
}

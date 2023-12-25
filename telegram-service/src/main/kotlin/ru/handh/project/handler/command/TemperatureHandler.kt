package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.handler.ActionHandler
import ru.handh.project.utils.isReply

@Component
class TemperatureHandler(
    private val commandHandlerHelper: CommandHandlerHelper
) : ActionHandler {
    override val type: ActionType = ActionType.TEMPERATURE

    override fun isTriggered(update: Update) =
        isReply(update.message?.replyToMessage?.text, DeviceCommand.TEMPERATURE.replyMessage())

    override fun handle(update: Update, authChat: ChatEntity?) {
        commandHandlerHelper.handleDeviceCommand(update, DeviceCommand.TEMPERATURE)
    }
}

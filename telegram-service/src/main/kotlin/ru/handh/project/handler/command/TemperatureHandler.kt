package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import ru.handh.project.dto.command.TemperatureData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler

@Component
class TemperatureHandler(
    private val commandHandlerHelper: CommandHandlerHelper
) : ActionHandler<TemperatureData> {
    override val type: ActionType = ActionType.TEMPERATURE

    override fun handle(data: TemperatureData, authChat: ChatEntity?) {
        commandHandlerHelper.handleDeviceCommand(data.chatId, data.messageId, data.messageText)
    }
}

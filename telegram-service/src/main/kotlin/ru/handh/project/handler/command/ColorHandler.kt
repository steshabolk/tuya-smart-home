package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import ru.handh.project.dto.command.ColorData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler

@Component
class ColorHandler(
    private val commandHandlerHelper: CommandHandlerHelper
) : ActionHandler<ColorData> {
    override val type: ActionType = ActionType.COLOR

    override fun handle(data: ColorData, authChat: ChatEntity?) {
        commandHandlerHelper.handleDeviceCommand(data.chatId, data.messageId, data.messageText)
    }
}

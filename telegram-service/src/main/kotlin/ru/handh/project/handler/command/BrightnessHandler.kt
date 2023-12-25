package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import ru.handh.project.dto.command.BrightnessData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler

@Component
class BrightnessHandler(
    private val commandHandlerHelper: CommandHandlerHelper
) : ActionHandler<BrightnessData> {
    override val type: ActionType = ActionType.BRIGHTNESS

    override fun handle(data: BrightnessData, authChat: ChatEntity?) {
        commandHandlerHelper.handleDeviceCommand(data.chatId, data.messageId, data.messageText)
    }
}

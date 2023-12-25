package ru.handh.project.handler.command

import org.springframework.stereotype.Component
import ru.handh.project.dto.command.SwitchLedData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler

@Component
class SwitchLedHandler(
    private val commandHandlerHelper: CommandHandlerHelper
) : ActionHandler<SwitchLedData> {
    override val type: ActionType = ActionType.SWITCH_LED

    override fun handle(data: SwitchLedData, authChat: ChatEntity?) {
        commandHandlerHelper.handleDeviceCommand(data.chatId, data.messageId, data.messageText)
    }
}

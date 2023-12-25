package ru.handh.project.dto.command

import ru.handh.project.dto.action.ActionData
import ru.handh.project.enum.ActionType

class SwitchLedData(
    chatId: Long,
    messageId: Int,
    val messageText: String
) : ActionData(chatId, messageId, ActionType.SWITCH_LED)

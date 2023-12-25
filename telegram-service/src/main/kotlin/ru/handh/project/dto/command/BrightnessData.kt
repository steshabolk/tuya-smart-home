package ru.handh.project.dto.command

import ru.handh.project.dto.action.ActionData
import ru.handh.project.enum.ActionType

class BrightnessData(
    chatId: Long,
    messageId: Int,
    val messageText: String
) : ActionData(chatId, messageId, ActionType.BRIGHTNESS)

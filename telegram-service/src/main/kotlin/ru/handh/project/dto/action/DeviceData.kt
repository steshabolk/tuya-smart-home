package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class DeviceData(
    chatId: Long,
    messageId: Int,
) : ActionData(chatId, messageId, ActionType.DEVICE)

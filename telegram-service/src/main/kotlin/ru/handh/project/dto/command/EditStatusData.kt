package ru.handh.project.dto.command

import ru.handh.project.dto.action.ActionData
import ru.handh.project.enum.ActionType

class EditStatusData(
    chatId: Long,
    messageId: Int,
) : ActionData(chatId, messageId, ActionType.EDIT_STATUS)

package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

abstract class ActionData(
    val chatId: Long,
    val messageId: Int,
    val actionType: ActionType
)


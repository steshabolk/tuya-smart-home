package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class StartData(
    chatId: Long,
    messageId: Int,
    val firstName:String
) : ActionData(chatId, messageId, ActionType.START)

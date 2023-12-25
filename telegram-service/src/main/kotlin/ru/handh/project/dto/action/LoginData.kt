package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class LoginData(
    chatId: Long,
    messageId: Int
) : ActionData(chatId, messageId, ActionType.LOGIN)

package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class LogoutData(
    chatId: Long,
    messageId: Int
) : ActionData(chatId, messageId, ActionType.LOGOUT)

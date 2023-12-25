package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class TokenAuthData(
    chatId: Long,
    messageId: Int,
    val messageText: String
) : ActionData(chatId, messageId, ActionType.TOKEN_AUTH)

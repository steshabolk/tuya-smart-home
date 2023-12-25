package ru.handh.project.dto.action

import ru.handh.project.enum.ActionType

class HomeData(
    chatId: Long,
    messageId: Int,
) : ActionData(chatId, messageId, ActionType.HOME)

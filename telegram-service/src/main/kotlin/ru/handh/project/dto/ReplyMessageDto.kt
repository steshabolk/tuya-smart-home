package ru.handh.project.dto

import ru.handh.project.enum.ActionType


class ReplyMessageDto(
    val messageId: Int,
    val actionType: ActionType
)

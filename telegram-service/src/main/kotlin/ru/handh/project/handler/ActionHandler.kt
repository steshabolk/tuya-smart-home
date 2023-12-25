package ru.handh.project.handler

import ru.handh.project.dto.action.ActionData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType

interface ActionHandler<D : ActionData> {
    val type: ActionType

    fun handle(data: D, authChat: ChatEntity? = null)
}

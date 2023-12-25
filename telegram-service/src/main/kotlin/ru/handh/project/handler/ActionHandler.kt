package ru.handh.project.handler

import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType

interface ActionHandler {
    val type: ActionType

    fun isTriggered(update: Update): Boolean

    fun handle(update: Update, authChat: ChatEntity? = null)
}

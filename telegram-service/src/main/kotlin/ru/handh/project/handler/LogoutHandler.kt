package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.service.ChatService

@Component
class LogoutHandler(
    private val chatService: ChatService,
    private val accountHandler: AccountHandler
) : ActionHandler {
    override val type: ActionType = ActionType.LOGOUT

    override fun isTriggered(update: Update) =
        update.message?.text == type.command

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chatId = update.message.chatId
        chatService.deleteByChatId(chatId)
        accountHandler.handle(update, null)
    }
}

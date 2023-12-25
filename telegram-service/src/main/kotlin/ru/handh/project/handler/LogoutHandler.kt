package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.AccountData
import ru.handh.project.dto.action.LogoutData
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.service.ChatService

@Component
class LogoutHandler(
    private val chatService: ChatService,
    private val accountHandler: AccountHandler
) : ActionHandler<LogoutData> {
    override val type: ActionType = ActionType.LOGOUT

    override fun handle(data: LogoutData, authChat: ChatEntity?) {
        chatService.deleteByChatId(data.chatId)
        accountHandler.handle(AccountData(chatId = data.chatId, messageId = data.messageId), null)
    }
}

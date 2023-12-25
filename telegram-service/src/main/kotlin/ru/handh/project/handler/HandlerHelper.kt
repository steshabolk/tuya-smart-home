package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.service.ChatService

@Component
class HandlerHelper(
    private val chatService: ChatService,
    private val accountHandler: AccountHandler,
) {

    fun getAuthChat(update: Update): ChatEntity? {
        val chatId = update.message.chatId
        val chat = chatService.findByChatId(chatId)
        if (chat == null) {
            accountHandler.handle(update, authChat = null)
        }
        return chat
    }
}

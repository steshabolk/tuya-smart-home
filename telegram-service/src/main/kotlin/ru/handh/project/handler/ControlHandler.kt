package ru.handh.project.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.sender.TelegramBotMessageSender
import ru.handh.project.service.ChatService
import ru.handh.project.utils.chooseHomeMessage
import ru.handh.project.utils.emptyHomesMessage
import ru.handh.project.utils.getMultilevelKeyboard
import ru.handh.project.utils.homeButton

@Component
class ControlHandler(
    private val telegramBotMessageSender: TelegramBotMessageSender,
    private val chatService: ChatService,
    private val handlerHelper: HandlerHelper
) : ActionHandler {
    override val type: ActionType = ActionType.CONTROL

    override fun isTriggered(update: Update) =
        update.message?.text == type.command

    override fun handle(update: Update, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(update) ?: return
        val chatId = update.message.chatId
        try {
            val homes = chatService.getHomes(chat.userId)
            if (homes.isEmpty()) {
                telegramBotMessageSender.sendMessage(
                    chatId = chatId,
                    text = emptyHomesMessage()
                )
                return
            }
            val keyboard = homes
                .map { home -> homeButton(home.id, home.name) }
                .getMultilevelKeyboard(2)
            telegramBotMessageSender.sendMessage(
                chatId = chatId,
                text = chooseHomeMessage(),
                buttons = keyboard
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = chatId)
        }
    }
}

package ru.handh.project.handler

import org.springframework.stereotype.Component
import ru.handh.project.dto.action.ControlData
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
) : ActionHandler<ControlData> {
    override val type: ActionType = ActionType.CONTROL

    override fun handle(data: ControlData, authChat: ChatEntity?) {
        val chat = handlerHelper.getAuthChat(chatId = data.chatId, messageId = data.messageId) ?: return
        try {
            val homes = chatService.getHomes(chat.userId)
            if (homes.isEmpty()) {
                telegramBotMessageSender.sendMessage(
                    chatId = data.chatId,
                    text = emptyHomesMessage()
                )
                return
            }
            val keyboard = homes
                .map { home -> homeButton(home.id, home.name) }
                .getMultilevelKeyboard(2)
            telegramBotMessageSender.sendMessage(
                chatId = data.chatId,
                text = chooseHomeMessage(),
                buttons = keyboard
            )
        } catch (ex: Exception) {
            telegramBotMessageSender.sendErrorMessage(chatId = data.chatId)
        }
    }
}

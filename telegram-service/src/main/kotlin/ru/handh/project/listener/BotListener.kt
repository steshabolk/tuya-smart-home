package ru.handh.project.listener

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import ru.handh.project.enum.ActionType
import ru.handh.project.handler.ActionHandler
import ru.handh.project.handler.StartHandler
import ru.handh.project.property.TelegramBotProperties


private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(value = ["telegram-bot.listener.enabled"], havingValue = "true")
class BotListener(
    private val telegramBotProperties: TelegramBotProperties,
    private val actionHandlers: List<ActionHandler>,
    private val startHandler: StartHandler
) : TelegramLongPollingBot(telegramBotProperties.token) {

    init {
        val commands = mutableListOf<BotCommand>()
        commands.add(BotCommand(ActionType.START.command, ActionType.START.description))
        commands.add(BotCommand(ActionType.CONTROL.command, ActionType.CONTROL.description))
        commands.add(BotCommand(ActionType.ACCOUNT.command, ActionType.ACCOUNT.description))
        this.execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
    }

    override fun getBotUsername(): String =
        telegramBotProperties.name

    override fun onUpdateReceived(update: Update) {
        val message = update.message ?: update.callbackQuery.message
        log.debug {
            "\nmessage{id=${message.messageId}, chat=${message.chatId}}\n" +
                    "user{id=${message.chat.id}, username=${message.chat.userName}}\n" +
                    "text=${update.message?.text}\n" +
                    "callback=${update.callbackQuery?.data}"
        }
        actionHandlers.firstOrNull { it.isTriggered(update) }
            ?.handle(update, null)
            ?: run {
                log.debug { "unknown command: message=${message.text}, reply=${message.replyToMessage.text}" }
                startHandler.handle(update, null)
            }
    }
}

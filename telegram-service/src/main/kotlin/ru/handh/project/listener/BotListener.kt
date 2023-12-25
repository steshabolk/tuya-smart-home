package ru.handh.project.listener

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import ru.handh.project.dto.action.AccountData
import ru.handh.project.dto.action.ActionData
import ru.handh.project.dto.action.ControlData
import ru.handh.project.dto.action.DeviceData
import ru.handh.project.dto.action.HomeData
import ru.handh.project.dto.action.LoginData
import ru.handh.project.dto.action.LogoutData
import ru.handh.project.dto.action.StartData
import ru.handh.project.dto.action.TokenAuthData
import ru.handh.project.dto.command.BrightnessData
import ru.handh.project.dto.command.ColorData
import ru.handh.project.dto.command.EditStatusData
import ru.handh.project.dto.command.SwitchLedData
import ru.handh.project.dto.command.TemperatureData
import ru.handh.project.enum.ActionType
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.enum.DeviceCommand.Companion.deviceCommands
import ru.handh.project.handler.ActionHandler
import ru.handh.project.property.TelegramBotProperties
import ru.handh.project.service.ChatService.Companion.getDeviceCommand
import ru.handh.project.service.ChatService.Companion.getReply
import ru.handh.project.service.ChatService.Companion.removeReply
import ru.handh.project.service.ChatService.Companion.setDevice
import ru.handh.project.service.ChatService.Companion.setDeviceCommand
import ru.handh.project.service.ChatService.Companion.setHome


private val log = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(value = ["telegram-bot.listener.enabled"], havingValue = "true")
class BotListener(
    private val telegramBotProperties: TelegramBotProperties,
    actionHandlers: List<ActionHandler<*>>
) : TelegramLongPollingBot(telegramBotProperties.token) {

    init {
        val commands = mutableListOf<BotCommand>()
        commands.add(BotCommand(ActionType.START.command, ActionType.START.description))
        commands.add(BotCommand(ActionType.CONTROL.command, ActionType.CONTROL.description))
        commands.add(BotCommand(ActionType.ACCOUNT.command, ActionType.ACCOUNT.description))
        this.execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
    }

    val actionHandlers = actionHandlers
        .filterIsInstance<ActionHandler<ActionData>>()
        .associateBy { it.type }

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
        val type = update.toActionType()
        val data = type.toActionData(message)
        actionHandlers[type]?.handle(data)
    }

    private fun Update.toActionType() =
        when (val command = message?.text ?: callbackQuery?.data) {
            ActionType.START.command -> ActionType.START
            ActionType.ACCOUNT.command -> ActionType.ACCOUNT
            ActionType.LOGIN.command -> ActionType.LOGIN
            ActionType.LOGOUT.command -> ActionType.LOGOUT
            ActionType.CONTROL.command -> ActionType.CONTROL
            else -> {
                if (command?.startsWith(ActionType.HOME.command) == true) {
                    setHome(command.replace(ActionType.HOME.command, "").toInt())
                    ActionType.HOME
                } else if (command?.startsWith(ActionType.DEVICE.command) == true) {
                    setDevice(command.replace(ActionType.DEVICE.command, "").toInt())
                    ActionType.DEVICE
                } else if (deviceCommands.contains(command)) {
                    setDeviceCommand(
                        messageId = callbackQuery.message.messageId,
                        command = DeviceCommand.from(command!!)!!
                    )
                    ActionType.EDIT_STATUS
                } else if (message.replyToMessage != null) {
                    when (message.replyToMessage.messageId) {
                        getReply()?.messageId -> {
                            val type = getReply()!!.actionType
                            removeReply()
                            type
                        }

                        getDeviceCommand()?.messageId -> {
                            val type = getDeviceCommand()!!.deviceCommand.actionType
                            type
                        }

                        else -> {
                            log.debug { "unknown reply: ${message.replyToMessage.messageId} : $command" }
                            ActionType.START
                        }
                    }
                } else {
                    log.debug { "unknown command: $command" }
                    ActionType.START
                }
            }
        }

    private fun ActionType.toActionData(message: Message): ActionData {
        return when (this) {
            ActionType.START -> StartData(
                chatId = message.chatId,
                messageId = message.messageId,
                firstName = message.chat.firstName
            )

            ActionType.ACCOUNT -> AccountData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.LOGIN -> LoginData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.LOGOUT -> LogoutData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.TOKEN_AUTH -> TokenAuthData(
                chatId = message.chatId,
                messageId = message.messageId,
                messageText = message.text.trim()
            )

            ActionType.CONTROL -> ControlData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.HOME -> HomeData(
                chatId = message.chatId,
                messageId = message.messageId,
            )

            ActionType.DEVICE -> DeviceData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.EDIT_STATUS -> EditStatusData(
                chatId = message.chatId,
                messageId = message.messageId
            )

            ActionType.SWITCH_LED -> SwitchLedData(
                chatId = message.chatId,
                messageId = message.messageId,
                messageText = message.text.trim()
            )

            ActionType.TEMPERATURE -> TemperatureData(
                chatId = message.chatId,
                messageId = message.messageId,
                messageText = message.text.trim()
            )

            ActionType.BRIGHTNESS -> BrightnessData(
                chatId = message.chatId,
                messageId = message.messageId,
                messageText = message.text.trim()
            )

            ActionType.COLOR -> ColorData(
                chatId = message.chatId,
                messageId = message.messageId,
                messageText = message.text.trim()
            )
        }
    }
}

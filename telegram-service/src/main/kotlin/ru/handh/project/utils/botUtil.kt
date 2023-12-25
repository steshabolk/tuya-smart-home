package ru.handh.project.utils

import com.vdurmont.emoji.EmojiParser
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.handh.device.client.model.DeviceDtoGen
import ru.handh.device.client.model.SimpleDeviceDtoGen
import ru.handh.project.enum.ActionType
import java.lang.Integer.min

fun inlineButton(text: String, callBack: String) =
    InlineKeyboardButton.builder()
        .text(text)
        .callbackData(callBack)
        .build()

fun forceReply() =
    ForceReplyKeyboard.builder()
        .forceReply(true)
        .build()

fun List<InlineKeyboardButton>.getMultilevelKeyboard(rowSize: Int): List<List<InlineKeyboardButton>> {
    val result = mutableListOf<List<InlineKeyboardButton>>()
    for (i in indices step rowSize) {
        result.add(subList(i, min(i + rowSize, size)))
    }
    return result
}

val LOGOUT_BUTTON =
    inlineButton(ActionType.LOGOUT.description, ActionType.LOGOUT.command)

val LOGIN_BUTTON =
    inlineButton(ActionType.LOGIN.description, ActionType.LOGIN.command)

fun homeButton(id: Int, name: String) =
    inlineButton(name, ActionType.HOME.command + id)

fun deviceButton(id: Int, idx: Int) =
    inlineButton(idx.toString(), ActionType.DEVICE.command + id)

const val emojiRobot = ":robot_face:"
const val emojiWriting = ":writing_hand|type_1_2:"
const val emojiErrorMark = ":x:"
const val emojiCheckMark = ":heavy_check_mark:"
const val emojiExclamation = ":heavy_exclamation_mark:"
const val emojiHouse = ":house_with_garden:"
const val emojiPointRight = ":point_right|type_1_2:"
const val emojiSquare = ":black_small_square:"

fun isReply(actual: String?, expected: String) =
    actual
        ?.run {
            EmojiParser.removeAllEmojis(this).contains(
                expected
                    .let { EmojiParser.parseToUnicode(it) }
                    .let { EmojiParser.removeAllEmojis(it).trim() }
            )
        } ?: false

fun errorMessage() =
    "$emojiExclamation We are sorry, an error has occurred. Please try again later"

fun startMessage(name: String) =
    "*Hi, $name!*\n" +
            "I am a device management bot $emojiRobot\n" +
            "My available commands:\n" +
            "*${ActionType.ACCOUNT.command}* - log in or log out of your account\n" +
            "*${ActionType.CONTROL.command}* - manage device status"

fun loggedInMessage() =
    "$emojiCheckMark You are logged in to your account"

fun notLoggedInMessage() =
    "$emojiErrorMark You are not logged in to your account"

fun loginMessage() =
    "$emojiWriting Please send your telegram token"

fun invalidTokenMessage() =
    "$emojiExclamation Your token is invalid. Let's try it again\n\n" + loginMessage()

fun emptyHomesMessage() =
    "$emojiErrorMark You don't have homes yet"

fun emptyDevicesMessage() =
    "$emojiErrorMark You don't have any devices in this home yet"

fun chooseHomeMessage() =
    "$emojiHouse Choose home:"

fun chooseDeviceMessage(devices: List<SimpleDeviceDtoGen>) =
    "$emojiPointRight Choose device:\n" +
            devices
                .mapIndexed { idx, device -> "${idx + 1}. *${device.name}* : ${device.category.lowercase()}" }
                .joinToString(separator = "\n")

fun deviceStatusMessage(device: DeviceDtoGen) =
    "$emojiCheckMark *${device.name}* : ${device.category.lowercase()}\n" +
            "Status:\n" +
            device.capabilities
                .joinToString(separator = "\n") {
                    "$emojiSquare ${
                        it.code!!.value.lowercase().replace("_", "-")
                    } : ${it.value}"
                }

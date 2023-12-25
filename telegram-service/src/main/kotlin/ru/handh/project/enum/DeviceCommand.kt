package ru.handh.project.enum

import ru.handh.device.client.model.CommandCodeGen
import ru.handh.project.dto.ColorHSV
import ru.handh.project.utils.emojiExclamation
import ru.handh.project.utils.emojiWriting
import ru.handh.project.utils.getMultilevelKeyboard
import ru.handh.project.utils.inlineButton

enum class DeviceCommand(
    val actionType: ActionType
) {
    SWITCH_LED(ActionType.SWITCH_LED) {
        override fun replyMessage(): String =
            "$emojiWriting enter true or false"

        override fun parseValue(value: String): Boolean? =
            when (value) {
                "true" -> true
                "false" -> false
                else -> null
            }
    },
    TEMPERATURE(ActionType.TEMPERATURE) {
        override fun replyMessage(): String =
            "$emojiWriting enter temperature (0;1000)"

        override fun parseValue(value: String): Int? =
            value.toIntOrNull()
                ?.let {
                    if ((0..1000).contains(it)) it
                    else null
                }
    },
    COLOR(ActionType.COLOR) {
        override fun replyMessage(): String =
            "$emojiWriting enter 3 numbers for color separated by a space in turn:\nhue (0;360), saturation (0;1000), value (0;1000)"

        override fun parseValue(value: String): ColorHSV? {
            val values = value.split(" ").mapNotNull { it.toIntOrNull() }
            if (values.size != 3 || !(0..360).contains(values[0]) || !(0..1000).contains(values[1]) || !(0..1000).contains(
                    values[2]
                )
            ) return null
            return ColorHSV(hue = values[0], saturation = values[1], value = values[2])
        }
    },
    BRIGHTNESS(ActionType.BRIGHTNESS) {
        override fun replyMessage(): String =
            "$emojiWriting enter brightness (10;1000)"

        override fun parseValue(value: String): Int? =
            value.toIntOrNull()
                ?.let {
                    if ((10..1000).contains(it)) it
                    else null
                }
    };

    abstract fun replyMessage(): String
    abstract fun parseValue(value: String): Any?

    val invalidCommandValueMessage =
        "$emojiExclamation Your value is invalid. Let's try it again\n\n" + this.replyMessage()

    companion object {

        private val convertMap: Map<DeviceCommand, CommandCodeGen> = mapOf(
            SWITCH_LED to CommandCodeGen.SWITCH_LED,
            TEMPERATURE to CommandCodeGen.TEMPERATURE,
            BRIGHTNESS to CommandCodeGen.BRIGHTNESS,
            COLOR to CommandCodeGen.COLOR
        )

        val deviceCommands = DeviceCommand.values().map { it.actionType.command }

        fun from(s: String): DeviceCommand? = DeviceCommand.values().find { s.startsWith(it.actionType.command) }

        fun commandsKeyboard(rowSize: Int, deviceId: Int) =
            CommandCodeGen.values().mapNotNull {
                it.toCommandButton(deviceId)
            }.getMultilevelKeyboard(rowSize)

        fun CommandCodeGen.toCommandButton(deviceId: Int) =
            convertMap.entries.associate { (k, v) -> v to k }[this]
                ?.run { inlineButton(this.actionType.description, this.actionType.command + deviceId) }

        fun DeviceCommand.toCommandCodeGen() =
            convertMap[this]
    }

}

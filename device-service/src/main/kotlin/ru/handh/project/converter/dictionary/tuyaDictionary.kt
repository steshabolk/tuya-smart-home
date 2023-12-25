package ru.handh.project.converter.dictionary

import ru.handh.project.enum.CommandCode
import ru.handh.project.exception.ApiError

private val tuyaDictionary: Map<CommandCode, String> =
    mapOf(
        CommandCode.SWITCH_LED to "switch_led",
        CommandCode.TEMPERATURE to "temp_value_v2",
        CommandCode.COLOR to "colour_data_v2",
        CommandCode.BRIGHTNESS to "bright_value_v2",
    )

fun CommandCode.toTuyaCode(): String =
    tuyaDictionary[this] ?: throw ApiError.UNKNOWN_COMMAND_CODE.toException(this)

fun String.toCommandCode(): CommandCode? =
    tuyaDictionary.entries.associate { (k, v) -> v to k }[this]


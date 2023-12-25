package ru.handh.project.enum

import io.swagger.v3.oas.annotations.media.Schema

@Schema(enumAsRef = true)
enum class CommandCode {
    SWITCH_LED,
    TEMPERATURE,
    COLOR,
    BRIGHTNESS;
}


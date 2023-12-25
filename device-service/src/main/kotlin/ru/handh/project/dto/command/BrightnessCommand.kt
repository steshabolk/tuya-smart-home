package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.enum.CommandCode
import ru.handh.project.util.BRIGHTNESS
import ru.handh.project.validator.Range

@JsonTypeName(BRIGHTNESS)
class BrightnessCommand(
    code: CommandCode,
    @field:Range(min = 10, max = 1000, field = "brightness")
    override val value: Int
) : Command(code)

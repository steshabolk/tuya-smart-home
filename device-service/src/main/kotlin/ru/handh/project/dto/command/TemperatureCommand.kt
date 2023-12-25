package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.enum.CommandCode
import ru.handh.project.util.TEMPERATURE
import ru.handh.project.validator.Range

@JsonTypeName(TEMPERATURE)
class TemperatureCommand(
    code: CommandCode,
    @field:Range(min = 0, max = 1000, field = "temperature")
    override val value: Int
) : Command(code)

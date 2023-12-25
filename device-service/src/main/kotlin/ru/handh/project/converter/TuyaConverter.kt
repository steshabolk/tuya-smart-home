package ru.handh.project.converter

import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import ru.handh.project.dto.command.Command
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.enum.CommandCode

@Validated
interface TuyaConverter<I : Command> {
    val code: CommandCode

    fun convert(@Valid data: I): TuyaCommand
}

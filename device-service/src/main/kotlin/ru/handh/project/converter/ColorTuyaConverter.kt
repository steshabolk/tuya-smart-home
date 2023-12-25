package ru.handh.project.converter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import ru.handh.project.converter.dictionary.toTuyaCode
import ru.handh.project.dto.command.ColorCommand
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.enum.CommandCode

@Component
class ColorTuyaConverter : TuyaConverter<ColorCommand> {

    private val mapper = jacksonObjectMapper()

    override fun convert(data: ColorCommand): TuyaCommand =
        data.run {
            TuyaCommand(
                code = code.toTuyaCode(),
                value = mapper.writeValueAsString(value)
            )
        }

    override val code: CommandCode = CommandCode.COLOR
}

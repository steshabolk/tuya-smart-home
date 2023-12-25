package ru.handh.project.converter

import org.springframework.stereotype.Component
import ru.handh.project.converter.dictionary.toTuyaCode
import ru.handh.project.dto.command.TemperatureCommand
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.enum.CommandCode

@Component
class TemperatureTuyaConverter : TuyaConverter<TemperatureCommand> {
    override fun convert(data: TemperatureCommand): TuyaCommand =
        data.run {
            TuyaCommand(
                code = code.toTuyaCode(),
                value = value
            )
        }

    override val code: CommandCode = CommandCode.TEMPERATURE
}

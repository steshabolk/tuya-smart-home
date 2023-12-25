package ru.handh.project.converter

import org.springframework.stereotype.Component
import ru.handh.project.converter.dictionary.toTuyaCode
import ru.handh.project.dto.command.BrightnessCommand
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.enum.CommandCode

@Component
class BrightnessTuyaConverter : TuyaConverter<BrightnessCommand> {
    override fun convert(data: BrightnessCommand): TuyaCommand =
        data.run {
            TuyaCommand(
                code = code.toTuyaCode(),
                value = value
            )
        }

    override val code: CommandCode = CommandCode.BRIGHTNESS
}

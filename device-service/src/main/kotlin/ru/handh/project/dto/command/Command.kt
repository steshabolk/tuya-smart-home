package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.handh.project.enum.CommandCode
import ru.handh.project.util.BRIGHTNESS
import ru.handh.project.util.COLOR
import ru.handh.project.util.SWITCH_LED
import ru.handh.project.util.TEMPERATURE

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "code",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SwitchLedCommand::class, name = SWITCH_LED),
    JsonSubTypes.Type(value = TemperatureCommand::class, name = TEMPERATURE),
    JsonSubTypes.Type(value = ColorCommand::class, name = COLOR),
    JsonSubTypes.Type(value = BrightnessCommand::class, name = BRIGHTNESS)
)
sealed class Command(
    val code: CommandCode
) {
    abstract val value: Any
}

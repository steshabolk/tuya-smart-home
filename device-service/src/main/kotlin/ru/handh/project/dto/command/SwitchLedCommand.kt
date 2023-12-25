package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.enum.CommandCode
import ru.handh.project.util.SWITCH_LED

@JsonTypeName(SWITCH_LED)
class SwitchLedCommand(
    code: CommandCode,
    override val value: Boolean
) : Command(code)

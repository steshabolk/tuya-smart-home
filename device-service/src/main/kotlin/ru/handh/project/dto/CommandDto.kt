package ru.handh.project.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import ru.handh.project.enum.CommandCode

class CommandDto (
    @field:Schema(nullable = true)
    @field:NotNull
    val code: CommandCode?,

    @field:Schema(nullable = true)
    @field:NotNull
    val value: Any?
)

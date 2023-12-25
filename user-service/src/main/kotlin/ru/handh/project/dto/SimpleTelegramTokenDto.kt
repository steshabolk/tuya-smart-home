package ru.handh.project.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class SimpleTelegramTokenDto(
    @field:NotNull
    @field:Size(min = 36, max = 36)
    val token: String?
)

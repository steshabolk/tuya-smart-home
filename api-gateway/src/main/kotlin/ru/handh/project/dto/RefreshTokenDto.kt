package ru.handh.project.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class RefreshTokenDto (
    @field:NotBlank
    @field:Size(min = 36, max = 36)
    val refreshToken: String?
)

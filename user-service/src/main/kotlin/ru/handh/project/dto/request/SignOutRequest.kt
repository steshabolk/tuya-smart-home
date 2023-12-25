package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class SignOutRequest(
    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 36, max = 36)
    val jwtId: String?
)

package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class DeleteAccountRequest (
    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 6, max = 30)
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{api.validation.password.message}")
    val password: String?
)

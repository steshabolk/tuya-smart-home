package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class HomeRequest(
    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 2, max = 64)
    val name: String?,

    @field:Schema(nullable = true)
    @field:Size(min = 2, max = 128)
    val address: String?
)

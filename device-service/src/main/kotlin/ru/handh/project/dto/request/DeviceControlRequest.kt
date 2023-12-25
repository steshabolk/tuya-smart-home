package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import ru.handh.project.dto.CommandDto

class DeviceControlRequest(
    @field:Schema(nullable = true)
    @field:Valid
    @field:NotEmpty
    val capabilities: List<CommandDto>?
)

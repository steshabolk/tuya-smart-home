package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.handh.project.validator.UniqueDevice

@UniqueDevice
class CreateDeviceRequest(
    @field:Schema(nullable = true)
    @field:NotBlank
    val tuyaDeviceId: String?,

    @field:Schema(nullable = true)
    @field:NotNull
    val homeId: Int?,

    @field:Schema(nullable = true)
    val roomId: Int?,

    @field:Schema(nullable = true)
    @field:Size(min = 2, max = 64)
    val name: String?
)

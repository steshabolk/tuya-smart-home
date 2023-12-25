package ru.handh.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ru.handh.project.validator.PasswordMatch
import ru.handh.project.validator.UniqueUsername

@PasswordMatch
@UniqueUsername
class RegisterRequest(
    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 2, max = 30)
    @field:Pattern(regexp = "^[\\p{L}\\d\\s_-]*$", message = "{api.validation.name.message}")
    val name: String?,

    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 2, max = 30)
    @field:Pattern(regexp = "^[a-zA-Z\\d_-]*$", message = "{api.validation.username.message}")
    val username: String?,

    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 6, max = 30)
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{api.validation.password.message}")
    val password: String?,

    @field:Schema(nullable = true)
    @field:NotNull
    @field:Size(min = 6, max = 30)
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{api.validation.password.message}")
    val confirmPassword: String?,
)

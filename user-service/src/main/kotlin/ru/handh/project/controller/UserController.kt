package ru.handh.project.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.handh.project.dto.SimpleTelegramTokenDto
import ru.handh.project.dto.TelegramTokenDto
import ru.handh.project.dto.TokenDto
import ru.handh.project.dto.request.AuthRequest
import ru.handh.project.dto.request.DeleteAccountRequest
import ru.handh.project.dto.request.RefreshRequest
import ru.handh.project.dto.request.RegisterRequest
import ru.handh.project.dto.request.SignOutRequest
import ru.handh.project.enum.KeyType
import ru.handh.project.enum.RsaType
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.service.JWKService
import ru.handh.project.service.UserService
import ru.handh.project.util.INVALID_INPUT_CODE
import ru.handh.project.util.INVALID_INPUT_DESCRIPTION
import ru.handh.project.util.JSON
import ru.handh.project.util.NOT_FOUND_CODE
import ru.handh.project.util.NOT_FOUND_DESCRIPTION
import ru.handh.project.util.SUCCESSFUL_CODE
import ru.handh.project.util.SUCCESSFUL_DESCRIPTION

@Tag(name = "User")
@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
    private val jwkService: JWKService,
) {

    @Operation(summary = "Register new user")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = TokenDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/register")
    fun register(@RequestBody @Valid registerRequest: RegisterRequest) =
        userService.register(registerRequest)

    @Operation(summary = "User authentication")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = TokenDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/auth")
    fun auth(@RequestBody @Valid authRequest: AuthRequest) =
        userService.auth(authRequest)

    @Operation(summary = "Refresh access token")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = TokenDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/refresh")
    fun refresh(@RequestParam(value = "userId", required = true) userId: Int,
                @RequestBody @Valid refreshRequest: RefreshRequest) =
        userService.refresh(userId, refreshRequest)

    @Operation(summary = "Sign out user")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/signout")
    fun signOut(@RequestBody @Valid signOutRequest: SignOutRequest) =
        userService.signOut(signOutRequest)

    @Operation(summary = "Delete user account")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @DeleteMapping("/account")
    fun deleteAccount(@RequestParam(value = "userId", required = true) userId: Int,
                      @RequestBody @Valid deleteAccountRequest: DeleteAccountRequest) =
        userService.deleteAccount(userId, deleteAccountRequest)

    @Hidden
    @GetMapping("/key")
    fun getRSAKey(@RequestParam(value = "keyType", required = true) keyType: KeyType,
                  @RequestParam(value = "rsaType", required = true) rsaType: RsaType
    ) =
        jwkService.getKeyByType(keyType, rsaType)

    @Operation(summary = "Generate telegram token")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = SimpleTelegramTokenDto::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @GetMapping("/telegram")
    fun getTelegramToken(@RequestParam(value = "userId", required = true) userId: Int) =
        userService.getTelegramToken(userId)

    @Operation(summary = "Authentication by telegram token")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = TelegramTokenDto::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/telegram")
    fun authByTelegramToken(@RequestBody @Valid telegramToken: SimpleTelegramTokenDto) =
        userService.authByTelegramToken(telegramToken)
}

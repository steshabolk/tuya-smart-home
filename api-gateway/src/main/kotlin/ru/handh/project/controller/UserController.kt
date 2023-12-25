package ru.handh.project.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.handh.project.client.UserClient
import ru.handh.project.dto.RefreshTokenDto
import ru.handh.project.service.GatewayService
import ru.handh.user.client.model.AuthRequestGen
import ru.handh.user.client.model.DeleteAccountRequestGen
import ru.handh.user.client.model.RefreshRequestGen
import ru.handh.user.client.model.RegisterRequestGen
import ru.handh.user.client.model.SignOutRequestGen
import ru.handh.user.client.model.SimpleTelegramTokenDtoGen

@RestController
@RequestMapping("/api")
class UserController(
    private val userClient: UserClient,
    private val gatewayService: GatewayService
) {

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequestGen) =
        userClient.register(registerRequest)

    @PostMapping("/auth")
    fun auth(@RequestBody authRequest: AuthRequestGen) =
        userClient.auth(authRequest)

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshTokenDto: RefreshTokenDto) =
        userClient.refresh(
            gatewayService.getUserId(),
            RefreshRequestGen(gatewayService.checkJwtIdsMatch(refreshTokenDto.refreshToken!!))
        )

    @PostMapping("/signout")
    fun signOut() =
        userClient.signOut(SignOutRequestGen(gatewayService.getJwtId()))

    @DeleteMapping("/account")
    fun deleteAccount(@RequestBody deleteAccountRequest: DeleteAccountRequestGen) =
        userClient.deleteAccount(gatewayService.getUserId(), deleteAccountRequest)

    @GetMapping("/telegram")
    fun getTelegramToken() =
        userClient.getTelegramToken(gatewayService.getUserId())

    @PostMapping("/telegram")
    fun authByTelegramToken(@RequestBody telegramToken: SimpleTelegramTokenDtoGen) =
        userClient.authByTelegramToken(telegramToken)
}

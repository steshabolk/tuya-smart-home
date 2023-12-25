package ru.handh.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import ru.handh.project.config.GlobalExceptionHandler
import ru.handh.project.dto.SimpleTelegramTokenDto
import ru.handh.project.dto.request.AuthRequest
import ru.handh.project.dto.request.DeleteAccountRequest
import ru.handh.project.dto.request.RefreshRequest
import ru.handh.project.dto.request.RegisterRequest
import ru.handh.project.dto.request.SignOutRequest
import ru.handh.project.enum.KeyType
import ru.handh.project.enum.RsaType
import ru.handh.project.repository.UserRepository
import ru.handh.project.service.JWKService
import ru.handh.project.service.UserService
import ru.handh.project.util.Messages
import ru.handh.project.util.deleteRequest
import ru.handh.project.util.doDeleteRequest
import ru.handh.project.util.doGetRequest
import ru.handh.project.util.doPostRequest
import ru.handh.project.util.expectEmptyResponse
import ru.handh.project.util.expectInternalApiError
import ru.handh.project.util.expectJson
import ru.handh.project.util.expectOkStatus
import ru.handh.project.util.expectValidationError
import ru.handh.project.util.refreshRequest
import ru.handh.project.util.refreshToken
import ru.handh.project.util.signOutRequest
import ru.handh.project.util.simpleTelegramTokenDto
import ru.handh.project.util.telegramTokenDto
import ru.handh.project.util.tokenDto
import ru.handh.project.util.userId
import ru.handh.project.util.userName
import ru.handh.project.util.userPassword
import ru.handh.project.util.userRequestMapping
import ru.handh.project.util.userUsername
import ru.handh.project.validator.PasswordMatch
import ru.handh.project.validator.UniqueUsername

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(UserController::class)
@ContextConfiguration(
    classes = [
        GlobalExceptionHandler::class,
        Messages::class,
        PasswordMatch::class,
        UniqueUsername::class
    ]
)
class UserControllerTest {

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var jwkService: JWKService

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @JvmStatic
        fun validRegister() =
            listOf(
                Arguments.of("name", userUsername, userPassword, userPassword),
                Arguments.of("name 1", userUsername, userPassword, userPassword),
                Arguments.of("name-1", userUsername, userPassword, userPassword),
                Arguments.of("name_1", userUsername, userPassword, userPassword),
                Arguments.of("пользователь", userUsername, userPassword, userPassword),
                Arguments.of("пользователь 1", userUsername, userPassword, userPassword),
                Arguments.of("пользователь-1", userUsername, userPassword, userPassword),
                Arguments.of("пользователь_1", userUsername, userPassword, userPassword)
            ) + listOf(
                Arguments.of(userName, "username", userPassword, userPassword),
                Arguments.of(userName, "username1", userPassword, userPassword),
                Arguments.of(userName, "username-1", userPassword, userPassword),
                Arguments.of(userName, "username_1", userPassword, userPassword)
            )

        @JvmStatic
        fun invalidRegister() =
            listOf(
                Arguments.of(null, userUsername, userPassword, userPassword),
                Arguments.of("a", userUsername, userPassword, userPassword),
                Arguments.of("a".repeat(31), userUsername, userPassword, userPassword),
                Arguments.of("name!", userUsername, userPassword, userPassword),
                Arguments.of("пользователь?", userUsername, userPassword, userPassword)
            ) + listOf(
                Arguments.of(userName, null, userPassword, userPassword),
                Arguments.of(userName, "a", userPassword, userPassword),
                Arguments.of(userName, "a".repeat(31), userPassword, userPassword),
                Arguments.of(userName, "username!", userPassword, userPassword),
                Arguments.of(userName, "user username", userPassword, userPassword)
            ) + listOf(
                Arguments.of(userName, userUsername, null, userPassword),
                Arguments.of(userName, userUsername, "Pass0", userPassword),
                Arguments.of(userName, userUsername, "A0" + "a".repeat(30), userPassword),
                Arguments.of(userName, userUsername, "password0", userPassword),
                Arguments.of(userName, userUsername, "Password", userPassword),
                Arguments.of(userName, userUsername, "P0", userPassword)
            ) + listOf(
                Arguments.of(userName, userUsername, "Password0", "Password01"),
                Arguments.of(userName, userUsername, userPassword, null),
                Arguments.of(userName, userUsername, userPassword, "Pass0"),
                Arguments.of(userName, userUsername, userPassword, "A0" + "a".repeat(30)),
                Arguments.of(userName, userUsername, userPassword, "password0"),
                Arguments.of(userName, userUsername, userPassword, "Password"),
                Arguments.of(userName, userUsername, userPassword, "P0")
            )

        @JvmStatic
        fun validAuth() =
            listOf(
                Arguments.of("username", userPassword),
                Arguments.of("username1", userPassword),
                Arguments.of("username-1", userPassword),
                Arguments.of("username_1", userPassword)
            )

        @JvmStatic
        fun invalidAuth() =
            listOf(
                Arguments.of(null, userPassword),
                Arguments.of("a", userPassword),
                Arguments.of("a".repeat(31), userPassword),
                Arguments.of("username!", userPassword),
                Arguments.of("user username", userPassword)
            ) + listOf(
                Arguments.of(userUsername, null),
                Arguments.of(userUsername, "Pass0"),
                Arguments.of(userUsername, "A0" + "a".repeat(30)),
                Arguments.of(userUsername, "password0"),
                Arguments.of(userUsername, "Password"),
                Arguments.of(userUsername, "P0")
            )

        @JvmStatic
        fun invalidUuidToken() =
            listOf(
                Arguments.of(null),
                Arguments.of(refreshToken + "0"),
            )

        @JvmStatic
        fun invalidPassword() =
            listOf(
                Arguments.of(null),
                Arguments.of("Pass0"),
                Arguments.of("A0" + "a".repeat(30)),
                Arguments.of("password0"),
                Arguments.of("Password"),
                Arguments.of("P0"),
            )

        @JvmStatic
        fun invalidGetRSAKeyParams() =
            listOf(
                Arguments.of(null, null),
                Arguments.of(null, RsaType.ENCRYPT),
                Arguments.of(KeyType.PRIVATE, null)
            )
    }

    private val userParams = mapOf("userId" to userId.toString())

    @Nested
    inner class Register {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#validRegister")
        fun success(name: String?, username: String?, password: String?, confirmPassword: String?) {

            val request = RegisterRequest(
                name = name,
                username = username,
                password = password,
                confirmPassword = confirmPassword
            )

            every { userRepository.existsByUsername(any()) } returns false
            every { userService.register(any()) } returns (tokenDto)

            mockMvc
                .doPostRequest(request, "$userRequestMapping/register", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectJson(tokenDto)
                }

            verify(exactly = 1) { userService.register(any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidRegister")
        fun `should throw an exception if validation failed`(
            name: String?,
            username: String?,
            password: String?,
            confirmPassword: String?
        ) {

            val request = RegisterRequest(
                name = name,
                username = username,
                password = password,
                confirmPassword = confirmPassword
            )

            every { userRepository.existsByUsername(any()) } returns false

            mockMvc
                .doPostRequest(request, "$userRequestMapping/register", emptyMap())
                .expectValidationError()
        }
    }

    @Nested
    inner class Auth {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#validAuth")
        fun success(username: String?, password: String?) {

            val request = AuthRequest(
                username = username,
                password = password,
            )

            every { userService.auth(any()) } returns (tokenDto)

            mockMvc
                .doPostRequest(request, "$userRequestMapping/auth", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectJson(tokenDto)
                }

            verify(exactly = 1) { userService.auth(any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidAuth")
        fun `should throw an exception if validation failed`(username: String?, password: String?) {

            val request = AuthRequest(
                username = username,
                password = password,
            )

            mockMvc
                .doPostRequest(request, "$userRequestMapping/auth", emptyMap())
                .expectValidationError()
        }
    }

    @Nested
    inner class RefreshToken {

        @Test
        fun success() {

            every { userService.refresh(userId, any()) } returns (tokenDto)

            mockMvc
                .doPostRequest(refreshRequest, "$userRequestMapping/refresh", userParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(tokenDto)
                }

            verify(exactly = 1) { userService.refresh(userId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doPostRequest(refreshRequest, "$userRequestMapping/refresh", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidUuidToken")
        fun `should throw an exception if validation failed`(jwtId: String?) {

            val request = RefreshRequest(
                jwtId = jwtId
            )

            mockMvc
                .doPostRequest(request, "$userRequestMapping/refresh", userParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class SignOut {

        @Test
        fun success() {

            every { userService.signOut(any()) } just Runs

            mockMvc
                .doPostRequest(signOutRequest, "$userRequestMapping/signout", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { userService.signOut(any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidUuidToken")
        fun `should throw an exception if validation failed`(jwtId: String?) {

            val request = SignOutRequest(
                jwtId = jwtId
            )

            mockMvc
                .doPostRequest(request, "$userRequestMapping/signout", emptyMap())
                .expectValidationError()
        }
    }

    @Nested
    inner class DeleteAccount {

        @Test
        fun success() {

            every { userService.deleteAccount(userId, any()) } just Runs

            mockMvc
                .doDeleteRequest(deleteRequest, "$userRequestMapping/account", userParams)
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { userService.deleteAccount(userId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doDeleteRequest(deleteRequest, "$userRequestMapping/account", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidPassword")
        fun `should throw an exception if validation failed`(password: String?) {

            val request = DeleteAccountRequest(
                password = password
            )

            mockMvc
                .doDeleteRequest(request, "$userRequestMapping/account", userParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class GetRSAKey {

        @Test
        fun success() {

            val params =
                mapOf("keyType" to KeyType.PRIVATE.name, "rsaType" to RsaType.ENCRYPT.name)

            every { jwkService.getKeyByType(any(), any()) } returns "dummy"

            mockMvc
                .doGetRequest("$userRequestMapping/key", params)
                .run {
                    this.expectOkStatus()
                }

            verify(exactly = 1) { jwkService.getKeyByType(any(), any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidGetRSAKeyParams")
        fun `should throw an exception if missing required params`(
            keyType: KeyType?,
            rsaType: RsaType?
        ) {

            val params = mutableMapOf<String, String>()

            keyType?.run {
                params["keyType"] = keyType.name
            }
            rsaType?.run {
                params["rsaType"] = rsaType.name
            }

            mockMvc
                .doGetRequest("$userRequestMapping/key", params)
                .expectInternalApiError()
        }
    }

    @Nested
    inner class GetTelegramToken {

        @Test
        fun success() {

            every { userService.getTelegramToken(userId) } returns (simpleTelegramTokenDto)

            mockMvc
                .doGetRequest("$userRequestMapping/telegram", userParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(simpleTelegramTokenDto)
                }

            verify(exactly = 1) { userService.getTelegramToken(userId) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doGetRequest("$userRequestMapping/telegram", emptyMap())
                .expectInternalApiError()
        }
    }

    @Nested
    inner class AuthByTelegramToken {

        @Test
        fun success() {

            every { userService.authByTelegramToken(any()) } returns (telegramTokenDto)

            mockMvc
                .doPostRequest(simpleTelegramTokenDto, "$userRequestMapping/telegram", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectJson(telegramTokenDto)
                }

            verify(exactly = 1) { userService.authByTelegramToken(any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.UserControllerTest#invalidUuidToken")
        fun `should throw an exception if validation failed`(token: String?) {

            val request = SimpleTelegramTokenDto(
                token = token
            )

            mockMvc
                .doPostRequest(request, "$userRequestMapping/telegram", emptyMap())
                .expectValidationError()
        }
    }
}

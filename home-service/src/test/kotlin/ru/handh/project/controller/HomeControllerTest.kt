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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import ru.handh.project.config.GlobalExceptionHandler
import ru.handh.project.dto.HomeDto
import ru.handh.project.dto.request.HomeRequest
import ru.handh.project.service.HomeService
import ru.handh.project.util.Messages
import ru.handh.project.util.doDeleteRequest
import ru.handh.project.util.doGetRequest
import ru.handh.project.util.doPostRequest
import ru.handh.project.util.doPutRequest
import ru.handh.project.util.expectEmptyResponse
import ru.handh.project.util.expectInternalApiError
import ru.handh.project.util.expectJson
import ru.handh.project.util.expectOkStatus
import ru.handh.project.util.expectValidationError
import ru.handh.project.util.homeAddress
import ru.handh.project.util.homeDto
import ru.handh.project.util.homeId
import ru.handh.project.util.homeName
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.homeRequest
import ru.handh.project.util.homeRequestMapping
import ru.handh.project.util.simpleHomeDto

@WebMvcTest
@Import(HomeController::class)
@ContextConfiguration(
    classes = [
        GlobalExceptionHandler::class,
        Messages::class
    ]
)
class HomeControllerTest {

    @MockkBean
    private lateinit var homeService: HomeService

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @JvmStatic
        fun validHomeRequest() = listOf(
            Arguments.of(homeName, homeAddress),
            Arguments.of(homeName, null)
        )

        @JvmStatic
        fun invalidHomeRequest() =
            listOf(
                Arguments.of(null, homeAddress),
                Arguments.of("", homeAddress),
                Arguments.of("a", homeAddress),
                Arguments.of("a".repeat(65), homeAddress)
            ) + listOf(
                Arguments.of(homeName, ""),
                Arguments.of(homeName, "a"),
                Arguments.of(homeName, "a".repeat(129))
            )
    }

    private val homeParams = mapOf("userId" to homeOwnerId.toString())

    @Nested
    inner class CreateHome {


        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.HomeControllerTest#validHomeRequest")
        fun success(name: String?, address: String?) {

            val request = HomeRequest(
                name = name!!,
                address = address
            )

            val expected = HomeDto(
                id = homeId,
                name = name,
                address = address,
                rooms = emptyList(),
            )

            every { homeService.createHome(homeOwnerId, any()) } returns (expected)

            mockMvc
                .doPostRequest(request, homeRequestMapping, homeParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(expected)
                }

            verify(exactly = 1) { homeService.createHome(homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doPostRequest(homeRequest, homeRequestMapping, emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.HomeControllerTest#invalidHomeRequest")
        fun `should throw an exception if validation failed`(name: String?, address: String?) {

            val request = HomeRequest(
                name = name,
                address = address
            )

            mockMvc
                .doPostRequest(request, homeRequestMapping, homeParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class EditHome {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.HomeControllerTest#validHomeRequest")
        fun success(name: String?, address: String?) {

            val request = HomeRequest(
                name = name!!,
                address = address
            )

            val expected = HomeDto(
                id = homeId,
                name = name,
                address = address,
                rooms = emptyList(),
            )

            every { homeService.editHome(homeId, homeOwnerId, any()) } returns (expected)

            mockMvc
                .doPutRequest(
                    request, "$homeRequestMapping/$homeId", homeParams
                )
                .run {
                    this.expectOkStatus()
                    this.expectJson(expected)
                }

            verify(exactly = 1) { homeService.editHome(homeId, homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doPutRequest(homeRequest, "$homeRequestMapping/$homeId", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.HomeControllerTest#invalidHomeRequest")
        fun `should throw an exception if validation failed`(name: String?, address: String?) {

            val request = HomeRequest(
                name = name,
                address = address
            )

            mockMvc
                .doPutRequest(request, "$homeRequestMapping/$homeId", homeParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class GetHome {

        @Test
        fun success() {

            every { homeService.getHome(homeId, homeOwnerId) } returns (homeDto)

            mockMvc
                .doGetRequest("$homeRequestMapping/$homeId", homeParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(homeDto)
                }

            verify(exactly = 1) { homeService.getHome(homeId, homeOwnerId) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doGetRequest("$homeRequestMapping/$homeId", emptyMap())
                .expectInternalApiError()
        }
    }

    @Nested
    inner class GetHomes {

        @Test
        fun success() {

            every { homeService.getHomes(homeOwnerId) } returns (listOf(simpleHomeDto))

            mockMvc
                .doGetRequest(homeRequestMapping, homeParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(listOf(simpleHomeDto))
                }

            verify(exactly = 1) { homeService.getHomes(homeOwnerId) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doGetRequest(homeRequestMapping, emptyMap())
                .expectInternalApiError()
        }
    }

    @Nested
    inner class DeleteHome {

        @Test
        fun success() {

            every { homeService.deleteHome(homeId) } just Runs

            mockMvc
                .doDeleteRequest("$homeRequestMapping/$homeId", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { homeService.deleteHome(homeId) }
        }
    }
}

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
import ru.handh.project.dto.request.RoomRequest
import ru.handh.project.service.RoomService
import ru.handh.project.util.Messages
import ru.handh.project.util.doDeleteRequest
import ru.handh.project.util.doPostRequest
import ru.handh.project.util.doPutRequest
import ru.handh.project.util.expectEmptyResponse
import ru.handh.project.util.expectInternalApiError
import ru.handh.project.util.expectJson
import ru.handh.project.util.expectOkStatus
import ru.handh.project.util.expectValidationError
import ru.handh.project.util.homeAddress
import ru.handh.project.util.homeId
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.roomDto
import ru.handh.project.util.roomId
import ru.handh.project.util.roomRequest
import ru.handh.project.util.roomRequestMapping

@WebMvcTest
@Import(RoomController::class)
@ContextConfiguration(
    classes = [
        GlobalExceptionHandler::class,
        Messages::class
    ]
)
class RoomControllerTest {

    @MockkBean
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @JvmStatic
        fun invalidRoomRequestParams() = listOf(
            Arguments.of(emptyMap<String, String>()),
            Arguments.of(mapOf("homeId" to homeId.toString())),
            Arguments.of(mapOf("userId" to homeOwnerId.toString()))
        )

        @JvmStatic
        fun invalidRoomRequest() =
            listOf(
                Arguments.of(null, homeAddress),
                Arguments.of("", homeAddress),
                Arguments.of("a", homeAddress),
                Arguments.of("a".repeat(65), homeAddress)
            )
    }

    @Nested
    inner class CreateRoom {

        private val createRoomParams = mapOf("homeId" to homeId.toString(), "userId" to homeOwnerId.toString())

        @Test
        fun success() {

            every { roomService.createRoom(homeId, homeOwnerId, any()) } returns (roomDto)

            mockMvc
                .doPostRequest(
                    roomRequest,
                    roomRequestMapping,
                    createRoomParams
                )
                .run {
                    this.expectOkStatus()
                    this.expectJson(roomDto)
                }

            verify(exactly = 1) { roomService.createRoom(homeId, homeOwnerId, any()) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.RoomControllerTest#invalidRoomRequestParams")
        fun `should throw an exception if missing required params`(params: Map<String, String>) {

            mockMvc
                .doPostRequest(roomRequest, roomRequestMapping, params)
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.RoomControllerTest#invalidRoomRequest")
        fun `should throw an exception if validation failed`(name: String?) {

            val request = RoomRequest(
                name = name,
            )

            mockMvc
                .doPostRequest(
                    request,
                    roomRequestMapping,
                    createRoomParams
                )
                .expectValidationError()
        }
    }

    @Nested
    inner class EditRoom {

        private val editRoomParams = mapOf("userId" to homeOwnerId.toString())

        @Test
        fun success() {

            every { roomService.editRoom(roomId, homeOwnerId, any()) } returns (roomDto)

            mockMvc
                .doPutRequest(roomRequest, "$roomRequestMapping/$roomId", editRoomParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(roomDto)
                }

            verify(exactly = 1) { roomService.editRoom(roomId, homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doPutRequest(roomRequest, "$roomRequestMapping/$roomId", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.RoomControllerTest#invalidRoomRequest")
        fun `should throw an exception if validation failed`(name: String?) {

            val request = RoomRequest(
                name = name,
            )

            mockMvc
                .doPutRequest(request, "$roomRequestMapping/$roomId", editRoomParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class DeleteRoom {

        @Test
        fun success() {

            every { roomService.deleteRoom(roomId) } just Runs

            mockMvc
                .doDeleteRequest("$roomRequestMapping/$roomId", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { roomService.deleteRoom(roomId) }
        }
    }
}

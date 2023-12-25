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
import ru.handh.project.dto.CommandDto
import ru.handh.project.dto.DeviceDto
import ru.handh.project.dto.request.CreateDeviceRequest
import ru.handh.project.dto.request.DeviceControlRequest
import ru.handh.project.dto.request.EditDeviceRequest
import ru.handh.project.enum.CommandCode
import ru.handh.project.repository.DeviceRepository
import ru.handh.project.service.DeviceService
import ru.handh.project.util.Messages
import ru.handh.project.util.brightnessDto
import ru.handh.project.util.colorDto
import ru.handh.project.util.deviceDto
import ru.handh.project.util.deviceId
import ru.handh.project.util.deviceName
import ru.handh.project.util.deviceRequestMapping
import ru.handh.project.util.doDeleteRequest
import ru.handh.project.util.doGetRequest
import ru.handh.project.util.doPostRequest
import ru.handh.project.util.doPutRequest
import ru.handh.project.util.expectEmptyResponse
import ru.handh.project.util.expectInternalApiError
import ru.handh.project.util.expectJson
import ru.handh.project.util.expectOkStatus
import ru.handh.project.util.expectValidationError
import ru.handh.project.util.homeId
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.roomId
import ru.handh.project.util.simpleDeviceDto
import ru.handh.project.util.switchLedDto
import ru.handh.project.util.temperatureDto
import ru.handh.project.util.tuyaDeviceId
import ru.handh.project.validator.UniqueDeviceValidator

@WebMvcTest
@Import(DeviceController::class)
@ContextConfiguration(
    classes = [
        GlobalExceptionHandler::class,
        Messages::class,
        UniqueDeviceValidator::class
    ]
)
class DeviceControllerTest {

    @MockkBean
    private lateinit var deviceService: DeviceService

    @MockkBean
    private lateinit var deviceRepository: DeviceRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        @JvmStatic
        fun validCreateDeviceArgs() = listOf(
            Arguments.of(tuyaDeviceId, homeId, roomId, deviceName),
            Arguments.of(tuyaDeviceId, homeId, null, deviceName),
            Arguments.of(tuyaDeviceId, homeId, roomId, null),
            Arguments.of(tuyaDeviceId, homeId, null, null)
        )

        @JvmStatic
        fun invalidCreateDeviceArgs() =
            listOf(
                Arguments.of(null, homeId, roomId, deviceName),
                Arguments.of("", homeId, roomId, deviceName),
                Arguments.of("    ", homeId, roomId, deviceName)
            ) + listOf(
                Arguments.of(tuyaDeviceId, null, roomId, deviceName)
            ) + listOf(
                Arguments.of(tuyaDeviceId, homeId, roomId, ""),
                Arguments.of(tuyaDeviceId, homeId, roomId, "a"),
                Arguments.of(tuyaDeviceId, homeId, roomId, "a".repeat(65))
            )

        @JvmStatic
        fun validEditDeviceArgs() = listOf(
            Arguments.of(homeId, roomId, deviceName),
            Arguments.of(homeId, null, deviceName)
        )

        @JvmStatic
        fun invalidEditDeviceArgs() =
            listOf(
                Arguments.of(homeId, roomId, ""),
                Arguments.of(homeId, roomId, "a"),
                Arguments.of(homeId, roomId, "a".repeat(65))
            ) + listOf(
                Arguments.of(null, roomId, deviceName)
            )

        @JvmStatic
        fun validGetDevicesParams() = listOf(
            Arguments.of(homeId, roomId, homeOwnerId),
            Arguments.of(homeId, null, homeOwnerId)
        )

        @JvmStatic
        fun invalidGetDevicesParams() = listOf(
            Arguments.of(null, roomId, homeOwnerId),
            Arguments.of(homeId, roomId, null)
        )

        @JvmStatic
        fun invalidEditDevicesStatusArgs() = listOf(
            Arguments.of(null),
            Arguments.of(emptyList<CommandDto>()),
            Arguments.of(listOf(CommandDto(null, null))),
            Arguments.of(listOf(CommandDto(null, 100))),
            Arguments.of(listOf(CommandDto(CommandCode.BRIGHTNESS, null)))
        )
    }

    private val deviceParams = mapOf("userId" to homeOwnerId.toString())

    @Nested
    inner class CreateDevice {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#validCreateDeviceArgs")
        fun success(tuyaDeviceId: String?, homeId: Int?, roomId: Int?, deviceName: String?) {

            val request = CreateDeviceRequest(
                tuyaDeviceId = tuyaDeviceId,
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            val expected = DeviceDto(
                id = 1,
                name = deviceName ?: "device-name-default",
                category = "light",
                capabilities = listOf(switchLedDto, temperatureDto, colorDto, brightnessDto)
            )

            every { deviceRepository.existsByTuyaDeviceId(tuyaDeviceId!!) } returns false
            every { deviceService.createDevice(homeOwnerId, any()) } returns (expected)

            mockMvc
                .doPostRequest(request, deviceRequestMapping, deviceParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(expected)
                }

            verify(exactly = 1) { deviceService.createDevice(homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            val request = CreateDeviceRequest(
                tuyaDeviceId = tuyaDeviceId,
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            mockMvc
                .doPostRequest(request, deviceRequestMapping, emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#invalidCreateDeviceArgs")
        fun `should throw an exception if validation failed`(
            tuyaDeviceId: String?,
            homeId: Int?,
            roomId: Int?,
            deviceName: String?
        ) {

            every { deviceRepository.existsByTuyaDeviceId(any()) } returns false

            val request = CreateDeviceRequest(
                tuyaDeviceId = tuyaDeviceId,
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            mockMvc
                .doPostRequest(request, deviceRequestMapping, deviceParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class EditDevice {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#validEditDeviceArgs")
        fun success(homeId: Int?, roomId: Int?, deviceName: String?) {

            val request = EditDeviceRequest(
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            val expected = DeviceDto(
                id = 1,
                name = deviceName ?: "device-name-default",
                category = "light",
                capabilities = listOf(switchLedDto, temperatureDto, colorDto, brightnessDto)
            )

            every { deviceService.editDevice(deviceId, homeOwnerId, any()) } returns (expected)

            mockMvc
                .doPutRequest(request, "$deviceRequestMapping/$deviceId", deviceParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(expected)
                }

            verify(exactly = 1) { deviceService.editDevice(deviceId, homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            val request = EditDeviceRequest(
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            mockMvc
                .doPutRequest(request, "$deviceRequestMapping/$deviceId", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#invalidEditDeviceArgs")
        fun `should throw an exception if validation failed`(homeId: Int?, roomId: Int?, deviceName: String?) {

            val request = EditDeviceRequest(
                homeId = homeId,
                roomId = roomId,
                name = deviceName
            )

            mockMvc
                .doPutRequest(request, "$deviceRequestMapping/$deviceId", deviceParams)
                .expectValidationError()
        }
    }

    @Nested
    inner class GetDevice {

        @Test
        fun success() {
            every { deviceService.getDevice(deviceId, homeOwnerId) } returns (deviceDto)

            mockMvc
                .doGetRequest("$deviceRequestMapping/$deviceId", deviceParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(deviceDto)
                }

            verify(exactly = 1) { deviceService.getDevice(deviceId, homeOwnerId) }

        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doGetRequest("$deviceRequestMapping/$deviceId", emptyMap())
                .expectInternalApiError()
        }
    }

    @Nested
    inner class DeleteDevice {

        @Test
        fun success() {

            every { deviceService.deleteDevice(deviceId) } just Runs

            mockMvc
                .doDeleteRequest("$deviceRequestMapping/$deviceId", emptyMap())
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { deviceService.deleteDevice(deviceId) }
        }
    }

    @Nested
    inner class GetDevices {

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#validGetDevicesParams")
        fun success(homeId: Int, roomId: Int?, homeOwnerId: Int) {

            val getDevicesParams = mutableMapOf(
                "homeId" to homeId.toString(),
                "userId" to homeOwnerId.toString()
            )
            roomId?.run {
                getDevicesParams["roomId"] = this.toString()
            }

            every { deviceService.getDevices(homeId, roomId, homeOwnerId) } returns (listOf(simpleDeviceDto))

            mockMvc
                .doGetRequest(deviceRequestMapping, getDevicesParams)
                .run {
                    this.expectOkStatus()
                    this.expectJson(listOf(simpleDeviceDto))
                }

            verify(exactly = 1) { deviceService.getDevices(homeId, roomId, homeOwnerId) }
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#invalidGetDevicesParams")
        fun `should throw an exception if missing required params`(homeId: Int?, roomId: Int?, homeOwnerId: Int?) {

            val getDevicesParams = mapOf(
                "homeId" to homeId.toString(),
                "roomId" to roomId.toString(),
                "userId" to homeOwnerId.toString()
            )

            mockMvc
                .doGetRequest(deviceRequestMapping, getDevicesParams)
                .expectInternalApiError()
        }
    }

    @Nested
    inner class EditDeviceStatus {

        private val request = DeviceControlRequest(
            capabilities = listOf(switchLedDto, temperatureDto, colorDto, brightnessDto)
        )

        @Test
        fun success() {


            every { deviceService.sendCommands(deviceId, homeOwnerId, any()) } just Runs

            mockMvc
                .doPostRequest(request, "$deviceRequestMapping/$deviceId/control", deviceParams)
                .run {
                    this.expectOkStatus()
                    this.expectEmptyResponse()
                }

            verify(exactly = 1) { deviceService.sendCommands(deviceId, homeOwnerId, any()) }
        }

        @Test
        fun `should throw an exception if missing required params`() {

            mockMvc
                .doPostRequest(request, "$deviceRequestMapping/$deviceId/control", emptyMap())
                .expectInternalApiError()
        }

        @ParameterizedTest
        @MethodSource("ru.handh.project.controller.DeviceControllerTest#invalidEditDevicesStatusArgs")
        fun `should throw an exception if validation failed`(capabilities: List<CommandDto>?) {

            val request = DeviceControlRequest(
                capabilities = capabilities
            )

            mockMvc
                .doPostRequest(request, "$deviceRequestMapping/$deviceId/control", deviceParams)
                .expectValidationError()
        }
    }
}

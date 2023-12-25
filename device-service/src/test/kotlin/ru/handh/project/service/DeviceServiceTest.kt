package ru.handh.project.service

import com.ninjasquad.springmockk.SpykBean
import com.tuya.connector.api.core.ConnectorFactory
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.converter.BrightnessTuyaConverter
import ru.handh.project.converter.ColorTuyaConverter
import ru.handh.project.converter.SwitchLedTuyaConverter
import ru.handh.project.converter.TemperatureTuyaConverter
import ru.handh.project.dto.CommandDto
import ru.handh.project.dto.command.ColorCommand
import ru.handh.project.dto.request.CreateDeviceRequest
import ru.handh.project.dto.request.DeviceControlRequest
import ru.handh.project.entity.DeviceEntity
import ru.handh.project.enum.CommandCode
import ru.handh.project.enum.DeviceCategory
import ru.handh.project.exception.ApiError
import ru.handh.project.repository.DeviceRepository
import ru.handh.project.util.DeviceConnectorConfig
import ru.handh.project.util.DeviceConnectorConfig.Companion.deviceDetails
import ru.handh.project.util.deviceEditRequest
import ru.handh.project.util.deviceEntity
import ru.handh.project.util.deviceEntityWithAnotherHomeId
import ru.handh.project.util.deviceEntityWithoutRoom
import ru.handh.project.util.deviceName
import ru.handh.project.util.deviceNotFoundId
import ru.handh.project.util.expectApiError
import ru.handh.project.util.homeId
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.roomId
import ru.handh.project.util.tuyaDeviceId

@Import(
    DeviceService::class,
    DeviceConnectorConfig::class,
    BrightnessTuyaConverter::class,
    ColorTuyaConverter::class,
    SwitchLedTuyaConverter::class,
    TemperatureTuyaConverter::class
)
@DataJpaTest
@EnableAutoConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class DeviceServiceTest {

    @MockBean
    private lateinit var connectorFactory: ConnectorFactory

    @SpykBean
    private lateinit var deviceRepository: DeviceRepository

    @Autowired
    private lateinit var deviceService: DeviceService

    @Autowired
    private lateinit var entityManager: EntityManager

    private val deviceEntitySlot = slot<DeviceEntity>()

    companion object {
        @JvmStatic
        fun validCreateDeviceRequest() = listOf(
            Arguments.of(tuyaDeviceId, homeId, roomId, deviceName),
            Arguments.of(tuyaDeviceId, homeId, roomId, null)
        )

        @JvmStatic
        fun validSendCommandsRequest() = listOf(
            Arguments.of(CommandCode.SWITCH_LED, true),
            Arguments.of(CommandCode.BRIGHTNESS, 10),
            Arguments.of(CommandCode.TEMPERATURE, 100),
            Arguments.of(
                CommandCode.COLOR, ColorCommand.ColorHSV(
                    hue = 255,
                    saturation = 400,
                    value = 600
                )
            )
        )
    }

    @Nested
    inner class CreateDevice {

        @ParameterizedTest
        @MethodSource("ru.handh.project.service.DeviceServiceTest#validCreateDeviceRequest")
        fun success(tuyaDeviceId: String, homeId: Int, roomId: Int, name: String?) {

            every { deviceRepository.save(capture(deviceEntitySlot)) } answers { callOriginal() }

            val request = CreateDeviceRequest(
                tuyaDeviceId = tuyaDeviceId,
                homeId = homeId,
                roomId = roomId,
                name = name
            )

            val device = deviceService.createDevice(homeOwnerId, request)

            assertTrue(deviceRepository.existsById(device.id))
            name
                ?.run {
                    assertEquals(this, deviceEntitySlot.captured.name)
                    assertEquals(this, device.name)
                }
                ?: run {
                    assertEquals(deviceDetails.name, deviceEntitySlot.captured.name)
                    assertEquals(deviceDetails.name, device.name)
                }
            assertEquals(DeviceCategory.LIGHT.name, device.category)
            assertEquals(4, device.capabilities.size)

            verify(exactly = 1) { deviceRepository.save(deviceEntitySlot.captured) }
            assertEquals(homeOwnerId, deviceEntitySlot.captured.ownerId)
            assertEquals(DeviceCategory.LIGHT, deviceEntitySlot.captured.category)
        }
    }

    @Nested
    inner class EditDevice {

        @Test
        fun success() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))
            val oldName = savedDevice.name

            every { deviceRepository.save(capture(deviceEntitySlot)) } answers { callOriginal() }

            val editedDevice = deviceService.editDevice(savedDevice.id, savedDevice.ownerId, deviceEditRequest)

            assertEquals(savedDevice.id, editedDevice.id)
            assertNotEquals(oldName, editedDevice.name)

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }

            assertEquals(savedDevice.id, deviceEntitySlot.captured.id)
            assertEquals(savedDevice.ownerId, deviceEntitySlot.captured.ownerId)
            assertEquals(editedDevice.name, deviceEntitySlot.captured.name)
            verify(exactly = 1) { deviceRepository.save(deviceEntitySlot.captured) }
        }

        @Test
        fun `should throw an exception if device is not found`() {

            assertFalse(deviceRepository.existsById(deviceNotFoundId))

            expectApiError(ApiError.DEVICE_NOT_FOUND) {
                deviceService.editDevice(deviceNotFoundId, homeOwnerId, deviceEditRequest)
            }
        }

        @Test
        fun `should throw an exception if device owner does not match`() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))
            assertNotEquals(2, savedDevice.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                deviceService.editDevice(savedDevice.id, 2, deviceEditRequest)
            }

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }
        }
    }

    @Nested
    inner class GetDevice {

        @Test
        fun success() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))

            val device = deviceService.getDevice(savedDevice.id, savedDevice.ownerId)

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }

            assertEquals(DeviceCategory.LIGHT.name, device.category)
            assertEquals(4, device.capabilities.size)
        }

        @Test
        fun `should throw an exception if device is not found`() {

            assertFalse(deviceRepository.existsById(deviceNotFoundId))


            expectApiError(ApiError.DEVICE_NOT_FOUND) {
                deviceService.getDevice(deviceNotFoundId, homeOwnerId)
            }
        }

        @Test
        fun `should throw an exception if device owner does not match`() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))
            assertNotEquals(2, savedDevice.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                deviceService.getDevice(savedDevice.id, 2)
            }

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }
        }

    }

    @Nested
    inner class DeleteDevice {

        @Test
        fun success() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))

            deviceService.deleteDevice(savedDevice.id)

            assertFalse(deviceRepository.existsById(savedDevice.id))

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }
            verify(exactly = 1) { deviceRepository.deleteById(savedDevice.id) }
        }

        @Test
        fun `should throw an exception if device is not found`() {

            assertFalse(deviceRepository.existsById(deviceNotFoundId))

            expectApiError(ApiError.DEVICE_NOT_FOUND) {
                deviceService.deleteDevice(deviceNotFoundId)
            }

            verify(exactly = 1) { deviceRepository.findById(deviceNotFoundId) }
            verify(exactly = 0) { deviceRepository.deleteById(deviceNotFoundId) }
        }
    }

    @Nested
    inner class GetDevices {

        @Test
        fun successWithRoom() {

            val savedFirst = deviceRepository.save(deviceEntity)
            deviceRepository.save(deviceEntityWithoutRoom)
            assertEquals(2, deviceRepository.findAll().size)

            val devices = deviceService.getDevices(savedFirst.homeId, savedFirst.roomId, savedFirst.ownerId)

            assertEquals(1, devices.size)
            assertEquals(savedFirst.id, devices[0].id)

            verify(exactly = 1) { deviceRepository.findAllByHomeIdAndRoomId(savedFirst.homeId, savedFirst.roomId!!) }
        }

        @Test
        fun successWithoutRoom() {

            val savedFirst = deviceRepository.save(deviceEntity)
            val savedSecond = deviceRepository.save(deviceEntityWithoutRoom)
            assertEquals(2, deviceRepository.findAll().size)

            val devices = deviceService.getDevices(savedFirst.homeId, null, savedFirst.ownerId)

            assertEquals(2, devices.size)
            assertEquals(savedFirst.id, devices[0].id)
            assertEquals(savedSecond.id, devices[1].id)

            verify(exactly = 1) { deviceRepository.findAllByHomeId(savedFirst.homeId) }
        }

        @Test
        fun `should throw an exception if device owner does not match`() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))
            assertNotEquals(2, savedDevice.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                deviceService.getDevices(savedDevice.homeId, null, 2)
            }
        }
    }

    @Nested
    inner class DeleteDeviceByHomeId {

        @Test
        fun success() {

            val savedFirst = deviceRepository.save(deviceEntity)
            val savedSecond = deviceRepository.save(deviceEntityWithoutRoom)
            val savedThird = deviceRepository.save(deviceEntityWithAnotherHomeId)
            assertEquals(3, deviceRepository.findAll().size)
            assertEquals(savedFirst.homeId, savedSecond.homeId)
            assertNotEquals(savedFirst.homeId, savedThird.homeId)

            deviceService.deleteByHomeId(savedFirst.homeId)

            assertEquals(1, deviceRepository.findAll().size)
            assertTrue(deviceRepository.existsById(savedThird.id))

            verify(exactly = 1) { deviceRepository.deleteAllByHomeId(savedFirst.homeId) }
        }
    }

    @Nested
    inner class ResetDeviceRoom {

        @Test
        fun success() {

            val device = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(device.id))
            assertEquals(roomId, device.roomId)

            deviceService.resetRoom(roomId)
            entityManager.refresh(device)

            assertEquals(null, device.roomId)

            verify(exactly = 1) { deviceRepository.resetRoom(roomId) }
        }
    }

    @Nested
    inner class SendCommands {

        @ParameterizedTest
        @MethodSource("ru.handh.project.service.DeviceServiceTest#validSendCommandsRequest")
        fun success(code: CommandCode, value: Any) {

            val device = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(device.id))

            val request = DeviceControlRequest(
                capabilities = listOf(CommandDto(code = code, value = value))
            )

            deviceService.sendCommands(device.id, device.ownerId, request)

            verify(exactly = 1) { deviceRepository.findById(device.id) }
        }

        @Test
        fun `should throw an exception if device is not found`() {

            assertFalse(deviceRepository.existsById(deviceNotFoundId))

            val request = DeviceControlRequest(
                capabilities = listOf(CommandDto(code = CommandCode.SWITCH_LED, value = true))
            )

            expectApiError(ApiError.DEVICE_NOT_FOUND) {
                deviceService.sendCommands(deviceNotFoundId, homeOwnerId, request)
            }
        }

        @Test
        fun `should throw an exception if device owner does not match`() {

            val savedDevice = deviceRepository.save(deviceEntity)
            assertTrue(deviceRepository.existsById(savedDevice.id))
            assertNotEquals(2, savedDevice.ownerId)

            val request = DeviceControlRequest(
                capabilities = listOf(CommandDto(code = CommandCode.SWITCH_LED, value = true))
            )

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                deviceService.sendCommands(savedDevice.id, 2, request)
            }

            verify(exactly = 1) { deviceRepository.findById(savedDevice.id) }
        }
    }

}

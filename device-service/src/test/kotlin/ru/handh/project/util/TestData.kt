package ru.handh.project.util

import ru.handh.project.dto.CommandDto
import ru.handh.project.dto.DeviceDto
import ru.handh.project.dto.SimpleDeviceDto
import ru.handh.project.dto.command.ColorCommand
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.EditDeviceRequest
import ru.handh.project.entity.DeviceEntity
import ru.handh.project.enum.CommandCode
import ru.handh.project.enum.DeviceCategory
import ru.handh.project.model.HomeDeletedEvent
import ru.handh.project.model.RoomDeletedEvent

// commands
val switchLedDto = CommandDto(
    code = CommandCode.SWITCH_LED,
    value = true
)
val temperatureDto = CommandDto(
    code = CommandCode.TEMPERATURE,
    value = 100
)
val colorDto = CommandDto(
    code = CommandCode.COLOR,
    value = ColorCommand.ColorHSV(
        hue = 100,
        saturation = 100,
        value = 100
    )
)
val brightnessDto = CommandDto(
    code = CommandCode.BRIGHTNESS,
    value = 100
)

// device service
const val deviceRequestMapping = "/api/devices"
const val tuyaDeviceId = "vdevo169902593083504"
const val deviceId = 1
const val deviceNotFoundId = 1000
const val homeId = 1
const val roomId = 1
const val deviceName = "device-1"
const val deviceNameEdited = "device-1-edited"
const val homeOwnerId = 1

val deviceDto = DeviceDto(
    id = 1,
    name = deviceName,
    category = "LIGHT",
    capabilities = listOf(switchLedDto, temperatureDto, colorDto, brightnessDto)
)
val simpleDeviceDto = SimpleDeviceDto(
    id = 1,
    name = deviceName,
    category = "LIGHT",
)
val deviceEntity = DeviceEntity(
    id = -1,
    tuyaDeviceId = tuyaDeviceId,
    ownerId = homeOwnerId,
    homeId = homeId,
    roomId = roomId,
    name = deviceName,
    category = DeviceCategory.LIGHT
)
val deviceEntityWithoutRoom = DeviceEntity(
    id = -1,
    tuyaDeviceId = "vdevo169902593083505",
    ownerId = homeOwnerId,
    homeId = homeId,
    roomId = null,
    name = "device-2",
    category = DeviceCategory.LIGHT
)
val deviceEntityWithAnotherHomeId = DeviceEntity(
    id = -1,
    tuyaDeviceId = "vdevo169902593083506",
    ownerId = 2,
    homeId = 2,
    roomId = null,
    name = "device-3",
    category = DeviceCategory.LIGHT
)
val deviceEditRequest = EditDeviceRequest(
    homeId = homeId,
    roomId = roomId,
    name = deviceNameEdited
)

// kafka
const val homeDeletedTopic = "home-deleted"
const val roomDeletedTopic = "room-deleted"

val homeDeletedEvent = HomeDeletedEvent(
    message = DeletedMessageDto(homeId)
)
val roomDeletedEvent = RoomDeletedEvent(
    message = DeletedMessageDto(roomId)
)

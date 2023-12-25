package ru.handh.project.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.connector.DeviceConnector
import ru.handh.project.converter.TuyaConverter
import ru.handh.project.converter.dictionary.toCommandCode
import ru.handh.project.dto.CommandDto
import ru.handh.project.dto.command.ColorCommand
import ru.handh.project.dto.command.Command
import ru.handh.project.dto.request.CreateDeviceRequest
import ru.handh.project.dto.request.DeviceControlRequest
import ru.handh.project.dto.request.EditDeviceRequest
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.dto.tuya.TuyaDevice
import ru.handh.project.dto.tuya.TuyaSendCommandsRequest
import ru.handh.project.entity.DeviceEntity
import ru.handh.project.enum.CommandCode
import ru.handh.project.enum.DeviceCategory
import ru.handh.project.exception.ApiError
import ru.handh.project.exception.ApiResponseStatus
import ru.handh.project.repository.DeviceRepository

private val log = KotlinLogging.logger {}

@Service
class DeviceService(
    private val deviceConnector: DeviceConnector,
    tuyaConverters: List<TuyaConverter<*>>,
    private val deviceRepository: DeviceRepository,
) {

    private val mapper = jacksonObjectMapper()

    private val tuyaConverters = tuyaConverters
        .filterIsInstance<TuyaConverter<Command>>()
        .associateBy { it.code }

    @Transactional
    fun createDevice(ownerId: Int, createDeviceRequest: CreateDeviceRequest) =
        saveDevice(ownerId, createDeviceRequest)
            .setDeviceCapabilities()
            .toDto()

    @Transactional
    fun editDevice(id: Int, ownerId: Int, editDeviceRequest: EditDeviceRequest) =
        deviceRepository.findByIdOrNull(id)
            ?.checkOwner(ownerId)
            ?.let {
                log.debug {
                    "update Device{id=${it.id}, name=${editDeviceRequest.name}, " +
                            "home={id=${editDeviceRequest.homeId}}, room={id=${editDeviceRequest.roomId}}}"
                }
                deviceRepository.save(editDeviceRequest.toEntity(it))
            }
            ?.setDeviceCapabilities()
            ?.toDto()
            ?: throw ApiError.DEVICE_NOT_FOUND.toException(id)

    @Transactional(readOnly = true)
    fun getDevice(id: Int, ownerId: Int) =
        deviceRepository.findByIdOrNull(id)
            ?.checkOwner(ownerId)
            ?.setDeviceCapabilities()
            ?.toDto()
            ?: throw ApiError.DEVICE_NOT_FOUND.toException(id)

    @Transactional
    fun deleteDevice(id: Int) =
        deviceRepository.findByIdOrNull(id)
            ?.run {
                log.info { "Device{id=$id} was deleted" }
                deviceRepository.deleteById(id)
            }
            ?: throw ApiError.DEVICE_NOT_FOUND.toException(id)

    @Transactional(readOnly = true)
    fun getDevices(homeId: Int, roomId: Int?, ownerId: Int) =
        (roomId?.run { deviceRepository.findAllByHomeIdAndRoomId(homeId, roomId) }
            ?: deviceRepository.findAllByHomeId(homeId))
            .map { it.checkOwner(ownerId) }
            .map { it.toSimpleDto() }

    @Transactional
    fun deleteByHomeId(homeId: Int) =
        deviceRepository.deleteAllByHomeId(homeId)
            .also { log.debug { "devices for Home{id=$homeId} was deleted" } }

    @Transactional
    fun resetRoom(roomId: Int) =
        deviceRepository.resetRoom(roomId)
            .also { log.debug { "Room{id=$roomId} has been reset" } }

    fun sendCommands(id: Int, ownerId: Int, request: DeviceControlRequest) {
        val commands = request.toCommands()
        val device = deviceRepository.findByIdOrNull(id)
            ?.checkOwner(ownerId)
            ?: throw ApiError.DEVICE_NOT_FOUND.toException(id)
        commands
            .map {
                tuyaConverters[it.code]?.convert(it) ?: throw ApiError.UNKNOWN_COMMAND_CODE.toException(it.code)
            }
            .let { deviceConnector.sendCommands(device.tuyaDeviceId, TuyaSendCommandsRequest(it)) }
            .also {
                if (!it) throw ApiResponseStatus(HttpStatus.BAD_REQUEST)
            }
    }

    private fun saveDevice(ownerId: Int, createDeviceRequest: CreateDeviceRequest) =
        deviceConnector.getDeviceDetails(createDeviceRequest.tuyaDeviceId)
            .run {
                createDeviceRequest.toEntity(this, ownerId)
            }
            .let {
                log.info { "save new Device{name=${it.name}, category=${it.category.name}}" }
                deviceRepository.save(it)
            }

    private fun CreateDeviceRequest.toEntity(tuyaDevice: TuyaDevice, ownerId: Int, id: Int = -1) =
        DeviceEntity(
            id = id,
            tuyaDeviceId = tuyaDeviceId!!,
            ownerId = ownerId,
            homeId = homeId!!,
            roomId = roomId,
            name = name ?: tuyaDevice.name,
            category = DeviceCategory.from(tuyaDevice.category)
        )

    private fun EditDeviceRequest.toEntity(entity: DeviceEntity) =
        DeviceEntity(
            id = entity.id,
            tuyaDeviceId = entity.tuyaDeviceId,
            ownerId = entity.ownerId,
            homeId = homeId!!,
            roomId = roomId,
            name = name!!,
            category = entity.category
        )

    private fun DeviceEntity.setDeviceCapabilities() =
        apply {
            capabilities = deviceConnector.getDeviceStatus(tuyaDeviceId)
                .mapNotNull { it.toDeviceCapability() }
        }

    private fun TuyaCommand.toDeviceCapability() =
        code.toCommandCode()
            ?.let {
                val converted = convertTuyaValue(it, value)
                CommandDto(
                    code = it,
                    value = converted
                )
            }

    fun convertTuyaValue(code: CommandCode, value: Any) =
        when (code) {
            CommandCode.SWITCH_LED -> convertToClass(value, Boolean::class.java)
            CommandCode.TEMPERATURE -> convertToClass(value, Int::class.java)
            CommandCode.COLOR -> convertToClass(value, ColorCommand.ColorHSV::class.java)
            CommandCode.BRIGHTNESS -> convertToClass(value, Int::class.java)
        }

    private fun DeviceControlRequest.toCommands() =
        capabilities!!.map { command -> mapper.convertValue(command, Command::class.java) }

    private fun convertToClass(value: Any, clazz: Class<*>) =
        mapper.convertValue(value, clazz)

    private fun DeviceEntity.checkOwner(ownerId: Int) =
        if (this.ownerId != ownerId) throw ApiError.OWNER_NOT_MATCH.toException(ownerId, id)
        else this
}

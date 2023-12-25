package ru.handh.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.handh.project.dto.DeviceDto
import ru.handh.project.dto.SimpleDeviceDto
import ru.handh.project.dto.request.CreateDeviceRequest
import ru.handh.project.dto.request.DeviceControlRequest
import ru.handh.project.dto.request.EditDeviceRequest
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.service.DeviceService
import ru.handh.project.util.INVALID_INPUT_CODE
import ru.handh.project.util.INVALID_INPUT_DESCRIPTION
import ru.handh.project.util.JSON
import ru.handh.project.util.NOT_FOUND_CODE
import ru.handh.project.util.NOT_FOUND_DESCRIPTION
import ru.handh.project.util.SUCCESSFUL_CODE
import ru.handh.project.util.SUCCESSFUL_DESCRIPTION

@Tag(name = "Device")
@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService
) {

    @Operation(summary = "Create new device")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = DeviceDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping
    fun createDevice(@RequestParam(value = "userId", required = true) ownerId: Int,
                     @RequestBody @Valid createDeviceRequest: CreateDeviceRequest) =
        deviceService.createDevice(ownerId, createDeviceRequest)

    @Operation(summary = "Edit device")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = DeviceDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PutMapping("/{deviceId}")
    fun editDevice(@PathVariable(value = "deviceId") id: Int,
                   @RequestParam(value = "userId", required = true) ownerId: Int,
                   @RequestBody @Valid editDeviceRequest: EditDeviceRequest) =
        deviceService.editDevice(id, ownerId, editDeviceRequest)

    @Operation(summary = "Get device by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = DeviceDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable(value = "deviceId") id: Int,
                  @RequestParam(value = "userId", required = true) ownerId: Int) =
        deviceService.getDevice(id, ownerId)

    @Operation(summary = "Delete device")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable(value = "deviceId") id: Int) =
        deviceService.deleteDevice(id)

    @Operation(summary = "Get list of devices by parameters: home id, room id, owner id")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, array = ArraySchema(schema = Schema(implementation = SimpleDeviceDto::class)))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @GetMapping
    fun getDevices(@RequestParam(value = "homeId", required = true) homeId: Int,
                   @RequestParam(value = "roomId", required = false) roomId: Int?,
                   @RequestParam(value = "userId", required = true) ownerId: Int) =
        deviceService.getDevices(homeId, roomId, ownerId)

    @Operation(summary = "Edit device status")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping("/{deviceId}/control")
    fun editDeviceStatus(@PathVariable(value = "deviceId") id: Int,
                         @RequestParam(value = "userId", required = true) ownerId: Int,
                         @RequestBody @Valid request: DeviceControlRequest) =
        deviceService.sendCommands(id, ownerId, request)
}

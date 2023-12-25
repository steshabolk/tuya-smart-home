package ru.handh.project.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.handh.device.client.model.CreateDeviceRequestGen
import ru.handh.device.client.model.DeviceControlRequestGen
import ru.handh.device.client.model.EditDeviceRequestGen
import ru.handh.project.client.DeviceClient
import ru.handh.project.service.GatewayService

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceClient: DeviceClient,
    private val gatewayService: GatewayService
) {

    @PostMapping
    fun createDevice(@RequestBody createDeviceRequest: CreateDeviceRequestGen) =
        deviceClient.createDevice(gatewayService.getUserId(), createDeviceRequest)

    @PutMapping("/{deviceId}")
    fun editDevice(@PathVariable(value = "deviceId") id: Int,
                   @RequestBody editDeviceRequest: EditDeviceRequestGen) =
        deviceClient.editDevice(id, gatewayService.getUserId(), editDeviceRequest)

    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable(value = "deviceId") id: Int) =
        deviceClient.getDevice(id, gatewayService.getUserId())

    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable(value = "deviceId") id: Int) =
        deviceClient.deleteDevice(id)

    @GetMapping
    fun getDevices(@RequestParam(value = "homeId", required = true) homeId: Int,
                   @RequestParam(value = "roomId", required = false) roomId: Int?) =
        deviceClient.getDevices(homeId, gatewayService.getUserId(), roomId)

    @PostMapping("/{deviceId}/control")
    fun editDeviceStatus(@PathVariable(value = "deviceId") id: Int,
                         @RequestBody body: DeviceControlRequestGen, ) =
        deviceClient.editDeviceStatus(id, gatewayService.getUserId(), body)
}

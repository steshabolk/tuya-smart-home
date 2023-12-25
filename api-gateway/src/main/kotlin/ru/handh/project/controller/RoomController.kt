package ru.handh.project.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.handh.home.client.model.RoomRequestGen
import ru.handh.project.client.RoomClient
import ru.handh.project.service.GatewayService

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomClient: RoomClient,
    private val gatewayService: GatewayService
) {

    @PostMapping
    fun createRoom(@RequestParam(value = "homeId", required = true) homeId: Int,
                   @RequestBody roomRequest: RoomRequestGen) =
        roomClient.createRoom(homeId, gatewayService.getUserId(), roomRequest)

    @PutMapping("/{roomId}")
    fun editRoom(@PathVariable(value = "roomId") id: Int,
                 @RequestBody roomRequest: RoomRequestGen,
                 request: HttpServletRequest) =
        roomClient.editRoom(id, gatewayService.getUserId(), roomRequest)

    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable(value = "roomId") id: Int) =
        roomClient.deleteRoom(id)
}

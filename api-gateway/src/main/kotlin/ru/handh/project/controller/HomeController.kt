package ru.handh.project.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.handh.home.client.model.HomeRequestGen
import ru.handh.project.client.HomeClient
import ru.handh.project.service.ApiGatewayService

@RestController
@RequestMapping("/api/homes")
class HomeController(
    private val homeClient: HomeClient,
    private val apiGatewayService: ApiGatewayService
) {

    @PostMapping
    fun createHome(@RequestBody homeRequest: HomeRequestGen) =
        homeClient.createHome(apiGatewayService.getUserId(), homeRequest)

    @PutMapping("/{homeId}")
    fun editHome(@PathVariable(value = "homeId") id: Int,
                 @RequestBody homeRequest: HomeRequestGen) =
        homeClient.editHome(id, apiGatewayService.getUserId(), homeRequest)

    @GetMapping("/{homeId}")
    fun getHome(@PathVariable(value = "homeId") id: Int) =
        homeClient.getHome(id, apiGatewayService.getUserId())

    @GetMapping
    fun getHomes() =
        homeClient.getHomes(apiGatewayService.getUserId())

    @DeleteMapping("/{homeId}")
    fun deleteHome(@PathVariable(value = "homeId") id: Int) =
        homeClient.deleteHome(id)
}

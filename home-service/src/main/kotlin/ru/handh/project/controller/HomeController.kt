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
import ru.handh.project.dto.HomeDto
import ru.handh.project.dto.SimpleHomeDto
import ru.handh.project.dto.request.HomeRequest
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.service.HomeService
import ru.handh.project.util.INVALID_INPUT_CODE
import ru.handh.project.util.INVALID_INPUT_DESCRIPTION
import ru.handh.project.util.JSON
import ru.handh.project.util.NOT_FOUND_CODE
import ru.handh.project.util.NOT_FOUND_DESCRIPTION
import ru.handh.project.util.SUCCESSFUL_CODE
import ru.handh.project.util.SUCCESSFUL_DESCRIPTION

@Tag(name = "Home")
@RestController
@RequestMapping("/api/homes")
class HomeController(
    private val homeService: HomeService,
) {

    @Operation(summary = "Create new home")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = HomeDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping
    fun createHome(@RequestParam(value = "userId", required = true) ownerId: Int,
                   @RequestBody @Valid homeRequest: HomeRequest) =
        homeService.createHome(ownerId, homeRequest)


    @Operation(summary = "Edit home")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = HomeDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PutMapping("/{homeId}")
    fun editHome(@PathVariable(value = "homeId") id: Int,
                 @RequestParam(value = "userId", required = true) ownerId: Int,
                 @RequestBody @Valid homeRequest: HomeRequest) =
        homeService.editHome(id, ownerId, homeRequest)

    @Operation(summary = "Get home by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = HomeDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @GetMapping("/{homeId}")
    fun getHome(@PathVariable(value = "homeId") id: Int,
                @RequestParam(value = "userId", required = true) ownerId: Int) =
        homeService.getHome(id, ownerId)

    @Operation(summary = "Get list of homes by owner id")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, array = ArraySchema(schema = Schema(implementation = SimpleHomeDto::class)))])
    ])
    @GetMapping
    fun getHomes(@RequestParam(value = "userId", required = true) ownerId: Int) =
        homeService.getHomes(ownerId)

    @Operation(summary = "Delete home")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @DeleteMapping("/{homeId}")
    fun deleteHome(@PathVariable(value = "homeId") id: Int) =
        homeService.deleteHome(id)
}

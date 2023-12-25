package ru.handh.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.handh.project.dto.RoomDto
import ru.handh.project.dto.request.RoomRequest
import ru.handh.project.exception.ExceptionResponse
import ru.handh.project.service.RoomService
import ru.handh.project.util.INVALID_INPUT_CODE
import ru.handh.project.util.INVALID_INPUT_DESCRIPTION
import ru.handh.project.util.JSON
import ru.handh.project.util.NOT_FOUND_CODE
import ru.handh.project.util.NOT_FOUND_DESCRIPTION
import ru.handh.project.util.SUCCESSFUL_CODE
import ru.handh.project.util.SUCCESSFUL_DESCRIPTION


@Tag(name = "Room")
@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService,
) {

    @Operation(summary = "Create new room")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = RoomDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PostMapping
    fun createRoom(@RequestParam(value = "homeId", required = true) homeId: Int,
                   @RequestParam(value = "userId", required = true) ownerId: Int,
                   @RequestBody @Valid roomRequest: RoomRequest) =
        roomService.createRoom(homeId, ownerId, roomRequest)

    @Operation(summary = "Edit room")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = RoomDto::class))]),
        ApiResponse(responseCode = INVALID_INPUT_CODE, description = INVALID_INPUT_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @PutMapping("/{roomId}")
    fun editRoom(@PathVariable(value = "roomId") id: Int,
                 @RequestParam(value = "userId", required = true) ownerId: Int,
                 @RequestBody @Valid roomRequest: RoomRequest) =
        roomService.editRoom(id, ownerId, roomRequest)

    @Operation(summary = "Delete room")
    @ApiResponses(value = [
        ApiResponse(responseCode = SUCCESSFUL_CODE, description = SUCCESSFUL_DESCRIPTION,
            content = [Content()]),
        ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION,
            content = [Content(mediaType = JSON, schema = Schema(implementation = ExceptionResponse::class))])
    ])
    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable(value = "roomId") id: Int) =
        roomService.deleteRoom(id)
}

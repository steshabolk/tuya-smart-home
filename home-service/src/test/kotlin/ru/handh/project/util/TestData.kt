package ru.handh.project.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.handh.project.dto.HomeDto
import ru.handh.project.dto.RoomDto
import ru.handh.project.dto.SimpleHomeDto
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.HomeRequest
import ru.handh.project.dto.request.RoomRequest
import ru.handh.project.entity.HomeEntity
import ru.handh.project.entity.OutboxMessageEntity
import ru.handh.project.model.HomeDeletedEvent
import ru.handh.project.model.RoomDeletedEvent
import ru.handh.project.model.UserDeletedEvent

private val mapper = jacksonObjectMapper()

// home service
const val homeRequestMapping = "/api/homes"
const val homeOwnerId = 1
const val homeId = 1
const val homeNotFoundId = 1000
const val homeName = "home-1"
const val homeNameEdited = "home-1-edited"
const val homeAddress = "Lenin st."

val homeRequest = HomeRequest(
    name = homeName,
    address = homeAddress
)
val homeEditRequest = HomeRequest(
    name = homeNameEdited,
    address = homeAddress
)

val homeDto = HomeDto(
    id = homeId,
    name = homeRequest.name!!,
    address = homeRequest.address,
    rooms = emptyList(),
)
val simpleHomeDto = SimpleHomeDto(
    id = homeDto.id,
    name = homeDto.name
)

val homeEntity = HomeEntity(
    id = -1,
    name = homeName,
    address = homeAddress,
    ownerId = homeOwnerId
)
val homeEntityWithAnotherOwner = HomeEntity(
    id = -1,
    name = "home-2",
    address = null,
    ownerId = 2
)

// room service
const val roomRequestMapping = "/api/rooms"
const val roomId = 1
const val roomName = "room-1"
const val roomNameEdited = "room-1-edited"

val roomRequest = RoomRequest(
    name = roomName
)
val roomDto = RoomDto(
    id = roomId,
    name = roomRequest.name!!
)
val roomEditRequest = RoomRequest(
    name = roomNameEdited
)

// kafka
const val homeDeletedTopic = "home-deleted"
const val roomDeletedTopic = "room-deleted"

val userDeletedEvent = UserDeletedEvent(
    message = DeletedMessageDto(homeOwnerId)
)

// outbox message service
val homeDeletedEvent = HomeDeletedEvent(
    message = DeletedMessageDto(homeId)
)
val homeDeletedMessage = OutboxMessageEntity(
    id = -1,
    topic = homeDeletedTopic,
    message = mapper.writeValueAsString(homeDeletedEvent)
)

val roomDeletedEvent = RoomDeletedEvent(
    message = DeletedMessageDto(roomId)
)
val roomDeletedMessage = OutboxMessageEntity(
    id = -1,
    topic = roomDeletedTopic,
    message = mapper.writeValueAsString(roomDeletedEvent)
)

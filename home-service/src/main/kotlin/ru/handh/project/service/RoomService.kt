package ru.handh.project.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.RoomRequest
import ru.handh.project.entity.HomeEntity
import ru.handh.project.entity.RoomEntity
import ru.handh.project.exception.ApiError
import ru.handh.project.model.RoomDeletedEvent
import ru.handh.project.repository.HomeRepository
import ru.handh.project.repository.RoomRepository

private val log = KotlinLogging.logger {}

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val homeRepository: HomeRepository,
    private val outboxMessageService: OutboxMessageService
) {

    @Transactional
    fun createRoom(homeId: Int, ownerId: Int, roomRequest: RoomRequest) =
        homeRepository.findByIdOrNull(homeId)
            ?.checkOwner(ownerId)
            ?.let { roomRequest.toEntity(home = it) }
            ?.run {
                log.info { "save new Room{name=${this.name}, home={id=${this.home.id}}}" }
                roomRepository.save(this)
            }
            ?.toDto()
            ?: throw ApiError.HOME_NOT_FOUND.toException(homeId)

    @Transactional
    fun editRoom(id: Int, ownerId: Int, roomRequest: RoomRequest) =
        roomRepository.findByIdOrNull(id)
            ?.also { it.home.checkOwner(ownerId) }
            ?.let {
                log.debug { "update Room{id=${it.id}, home={id=${it.home.id}}}" }
                roomRepository.save(roomRequest.toEntity(id = id, home = it.home))
            }
            ?.toDto()
            ?: throw ApiError.ROOM_NOT_FOUND.toException(id)

    @Transactional
    fun deleteRoom(id: Int) =
        roomRepository.findByIdOrNull(id)
            ?.run {
                log.info { "Room{id=$id} was deleted" }
                roomRepository.deleteById(id) }
            ?.also {
                outboxMessageService.saveMessage(RoomDeletedEvent(DeletedMessageDto(id)))
            }
            ?: throw ApiError.ROOM_NOT_FOUND.toException(id)

    private fun RoomRequest.toEntity(id: Int = -1, home: HomeEntity) =
        RoomEntity(
            id = id,
            name = name!!,
            home = home
        )
}

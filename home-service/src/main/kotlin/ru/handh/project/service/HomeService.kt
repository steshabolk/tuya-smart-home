package ru.handh.project.service

import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.dto.request.HomeRequest
import ru.handh.project.entity.HomeEntity
import ru.handh.project.exception.ApiError
import ru.handh.project.model.HomeDeletedEvent
import ru.handh.project.repository.HomeRepository

private val log = KotlinLogging.logger {}

@Service
class HomeService(
    private val homeRepository: HomeRepository,
    private val outboxMessageService: OutboxMessageService
) {

    @Transactional
    fun createHome(ownerId: Int, homeRequest: HomeRequest) =
        homeRequest.toEntity(ownerId)
            .let {
                log.info { "save new Home{name=${homeRequest.name}, user id=$ownerId}" }
                homeRepository.save(it)
            }.toDto()

    @Transactional
    fun editHome(id: Int, ownerId: Int, homeRequest: HomeRequest) =
        homeRepository.findByIdOrNull(id)
            ?.checkOwner(ownerId)
            ?.let {
                log.debug { "update Home{id=${it.id}}" }
                homeRepository.save(homeRequest.toEntity(ownerId, id))
            }
            ?.toDto()
            ?: throw ApiError.HOME_NOT_FOUND.toException(id)

    @Transactional(readOnly = true)
    fun getHome(id: Int, ownerId: Int) =
        homeRepository.findWithRoomsById(id)
            ?.checkOwner(ownerId)
            ?.toDto()
            ?: throw ApiError.HOME_NOT_FOUND.toException(id)

    @Transactional(readOnly = true)
    fun getHomes(ownerId: Int) =
        homeRepository.findAllByOwnerId(ownerId)
            .map { it.toSimpleDto() }

    @Transactional
    fun deleteHome(id: Int) =
        homeRepository.findByIdOrNull(id)
            ?.run {
                log.info { "Home{id=$id} was deleted" }
                homeRepository.deleteById(id) }
            ?.also {
                outboxMessageService.saveMessage(HomeDeletedEvent(DeletedMessageDto(id)))
            }
            ?: throw ApiError.HOME_NOT_FOUND.toException(id)

    @Transactional
    fun deleteByOwnerId(ownerId: Int) =
        homeRepository.findAllByOwnerId(ownerId)
            .map { home -> home.id }
            .also {
                log.info { "homes{ids=$it} were deleted for User{id=$ownerId}" }
                homeRepository.deleteAllByIdInBatch(it)
            }
            .forEach { id -> outboxMessageService.saveMessage(HomeDeletedEvent(DeletedMessageDto(id))) }

    private fun HomeRequest.toEntity(ownerId: Int, id: Int = -1) =
        HomeEntity(
            id = id,
            name = name!!,
            address = address,
            ownerId = ownerId
        )
}

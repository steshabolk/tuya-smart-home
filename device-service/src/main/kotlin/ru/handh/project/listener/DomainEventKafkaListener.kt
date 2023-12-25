package ru.handh.project.listener

import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.handh.project.model.HomeDeletedEvent
import ru.handh.project.model.RoomDeletedEvent
import ru.handh.project.service.DeviceService

@Component
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class DomainEventKafkaListener(
    private val deviceService: DeviceService
) {

    @KafkaListener(topics = ["\${kafka.topics.home-deleted.name}"], containerFactory = "kafkaDomainEventListenerContainerFactory")
    fun listen(@Valid event: HomeDeletedEvent) =
        deviceService.deleteByHomeId(event.message.id)

    @KafkaListener(topics = ["\${kafka.topics.room-deleted.name}"], containerFactory = "kafkaDomainEventListenerContainerFactory")
    fun listen(@Valid event: RoomDeletedEvent) =
        deviceService.resetRoom(event.message.id)
}

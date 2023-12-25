package ru.handh.project.listener

import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.handh.project.model.UserDeletedEvent
import ru.handh.project.service.HomeService

@Component
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class DomainEventKafkaListener(
    private val homeService: HomeService
) {

    @KafkaListener(
        topics = ["\${kafka.topics.user-deleted.name}"], containerFactory = "kafkaDomainEventListenerContainerFactory"
    )
    fun listen(@Valid event: UserDeletedEvent) =
        homeService.deleteByOwnerId(event.message.id)
}

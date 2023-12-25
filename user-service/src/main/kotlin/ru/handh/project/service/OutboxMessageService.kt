package ru.handh.project.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.project.config.KafkaTopicsProperties
import ru.handh.project.entity.OutboxMessageEntity
import ru.handh.project.enum.DomainEventType
import ru.handh.project.exception.ApiError
import ru.handh.project.model.DomainEvent
import ru.handh.project.repository.OutboxMessageRepository

private val log = KotlinLogging.logger {}

@Service
class OutboxMessageService(
    private val outboxMessageRepository: OutboxMessageRepository,
    private val kafkaTopicsProperties: KafkaTopicsProperties
) {

    private val mapper = jacksonObjectMapper()

    @Transactional
    fun saveMessage(event: DomainEvent) =
        outboxMessageRepository.save(event.toEntity())
            .also {
                log.debug { "save new outbox Message{topic=${it.topic}, message=${it.message}}" }
            }

    @Transactional(readOnly = true)
    fun getAllMessages() = outboxMessageRepository.findAll()

    @Transactional
    fun deleteMessages(ids: List<Int>) =
        outboxMessageRepository.deleteAllByIdInBatch(ids)
            .also {
                log.debug { "deleted outbox messages{ids=$ids}" }
            }

    private fun DomainEvent.toEntity(id: Int = -1) =
        OutboxMessageEntity(
            id = id,
            topic = type.toTopicName(),
            message = toJsonString()
        )

    private fun DomainEvent.toJsonString() =
        mapper.writeValueAsString(this)

    private fun DomainEventType.toTopicName() =
        kafkaTopicsProperties.topics[this.topicPropName]?.name ?: throw ApiError.TOPIC_NOT_FOUND.toException(this.name)
}

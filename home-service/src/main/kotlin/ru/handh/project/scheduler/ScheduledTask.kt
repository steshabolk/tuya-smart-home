package ru.handh.project.scheduler

import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.handh.project.service.OutboxMessageService

private val log = KotlinLogging.logger {}

@Component
class ScheduledTask(
    private val outboxMessageService: OutboxMessageService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val batchSize = 100

    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "sendMessageToKafka")
    fun sendMessageToKafka() {
        log.debug { "send message to Kafka task executed" }
        val messages = outboxMessageService.getAllMessages()
        for (i in 0 until messages.size step batchSize) {
            messages.subList(i, minOf(i + batchSize, messages.size))
                .mapNotNull {
                    try {
                        kafkaTemplate.send(it.topic, it.message)
                        log.debug { "sent message to Kafka{topic=${it.topic}, message=${it.message}}" }
                        it.id
                    } catch (ex: Exception) {
                        log.error("failed to send message to Kafka{topic=${it.topic}}\n" +
                                "${ex.message}: ${ex.cause}")
                        null
                    }
                }
                .also {
                    outboxMessageService.deleteMessages(it)
                }
        }
    }
}

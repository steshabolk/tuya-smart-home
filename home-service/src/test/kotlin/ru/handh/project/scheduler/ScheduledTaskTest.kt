package ru.handh.project.scheduler

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.config.KafkaConfig
import ru.handh.project.config.KafkaTopicsProperties
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.repository.OutboxMessageRepository
import ru.handh.project.service.OutboxMessageService
import ru.handh.project.util.homeDeletedMessage
import ru.handh.project.util.roomDeletedMessage

@Import(ScheduledTask::class, KafkaConfig::class)
@DataJpaTest
@EnableConfigurationProperties(value = [KafkaTopicsProperties::class])
@EmbeddedKafka
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class ScheduledTaskTest {

    @SpykBean
    private lateinit var outboxMessageService: OutboxMessageService

    @SpykBean
    private lateinit var outboxMessageRepository: OutboxMessageRepository

    @SpykBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var scheduledTask: ScheduledTask

    @Nested
    inner class SendMessageToKafka {

        @Test
        fun success() {

            val topics = mutableListOf<String>()
            val messages = mutableListOf<String>()

            val savedFirst = outboxMessageRepository.save(homeDeletedMessage)
            val savedSecond = outboxMessageRepository.save(roomDeletedMessage)
            assertEquals(2, outboxMessageRepository.findAll().size)

            every { kafkaTemplate.send(capture(topics), capture(messages)) } answers { callOriginal() }

            scheduledTask.sendMessageToKafka()

            assertEquals(homeDeletedMessage.topic, topics[0])
            assertEquals(roomDeletedMessage.topic, topics[1])

            verify(exactly = 1) { outboxMessageService.getAllMessages() }
            verify(exactly = 1) { outboxMessageService.deleteMessages(listOf(savedFirst.id, savedSecond.id)) }
        }

    }
}

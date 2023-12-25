package ru.handh.project.scheduler

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import ru.handh.project.service.TokenService
import ru.handh.project.util.userDeletedMessage

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

    @MockkBean
    private lateinit var tokenService: TokenService

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

            val topic = slot<String>()
            val message = slot<String>()

            val deletedEvent = outboxMessageRepository.save(userDeletedMessage)
            assertTrue(outboxMessageRepository.existsById(deletedEvent.id))

            every { kafkaTemplate.send(capture(topic), capture(message)) } answers { callOriginal() }

            scheduledTask.sendMessageToKafka()

            assertEquals(userDeletedMessage.topic, topic.captured)

            verify(exactly = 1) { outboxMessageService.getAllMessages() }
            verify(exactly = 1) { outboxMessageService.deleteMessages(listOf(deletedEvent.id)) }
        }

    }
}

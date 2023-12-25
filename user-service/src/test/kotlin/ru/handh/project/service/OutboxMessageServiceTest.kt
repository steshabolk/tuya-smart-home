package ru.handh.project.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.config.KafkaTopicsProperties
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.entity.OutboxMessageEntity
import ru.handh.project.repository.OutboxMessageRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.userDeletedEvent
import ru.handh.project.util.userDeletedMessage
import ru.handh.project.util.userDeletedTopic

@Import(OutboxMessageService::class)
@DataJpaTest
@EnableConfigurationProperties(value = [KafkaTopicsProperties::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class OutboxMessageServiceTest {

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @SpykBean
    private lateinit var outboxMessageRepository: OutboxMessageRepository

    @Autowired
    private lateinit var outboxMessageService: OutboxMessageService

    private val mapper = jacksonObjectMapper()
    private val outboxMessageEntitySlot = slot<OutboxMessageEntity>()

    @Nested
    inner class SaveMessage {

        @Test
        fun success() {

            every { outboxMessageRepository.save(capture(outboxMessageEntitySlot)) } answers { callOriginal() }

            val outboxMessage = outboxMessageService.saveMessage(userDeletedEvent)

            assertTrue(outboxMessageRepository.existsById(outboxMessage.id))
            assertEquals(userDeletedTopic, outboxMessage.topic)
            assertEquals(mapper.writeValueAsString(userDeletedEvent), outboxMessage.message)

            assertEquals(userDeletedTopic, outboxMessageEntitySlot.captured.topic)
            verify(exactly = 1) { outboxMessageRepository.save(outboxMessageEntitySlot.captured) }
        }
    }

    @Nested
    inner class GetMessages {

        @Test
        fun success() {

            outboxMessageRepository.save(userDeletedMessage)

            val outboxMessages = outboxMessageService.getAllMessages()

            assertEquals(1, outboxMessages.size)
        }
    }

    @Nested
    inner class DeleteMessages {

        @Test
        fun success() {

            val message = outboxMessageRepository.save(userDeletedMessage)
            assertEquals(1, outboxMessageRepository.findAll().size)

            outboxMessageService.deleteMessages(listOf(message.id))

            assertEquals(0, outboxMessageRepository.findAll().size)
            verify(exactly = 1) { outboxMessageRepository.deleteAllByIdInBatch(listOf(message.id)) }
        }
    }
}

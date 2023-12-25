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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.config.KafkaTopicsProperties
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.entity.OutboxMessageEntity
import ru.handh.project.model.DomainEvent
import ru.handh.project.repository.OutboxMessageRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.homeDeletedEvent
import ru.handh.project.util.homeDeletedMessage
import ru.handh.project.util.homeDeletedTopic
import ru.handh.project.util.roomDeletedEvent
import ru.handh.project.util.roomDeletedMessage
import ru.handh.project.util.roomDeletedTopic

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

    companion object {
        @JvmStatic
        fun domainEvents() = listOf(
            Arguments.of(homeDeletedEvent, homeDeletedTopic),
            Arguments.of(roomDeletedEvent, roomDeletedTopic)
        )
    }

    @Nested
    inner class SaveMessage {

        @ParameterizedTest
        @MethodSource("ru.handh.project.service.OutboxMessageServiceTest#domainEvents")
        fun success(event: DomainEvent, topic: String) {

            every { outboxMessageRepository.save(capture(outboxMessageEntitySlot)) } answers { callOriginal() }

            val outboxMessage = outboxMessageService.saveMessage(event)

            assertTrue(outboxMessageRepository.existsById(outboxMessage.id))
            assertEquals(topic, outboxMessage.topic)
            assertEquals(mapper.writeValueAsString(event), outboxMessage.message)

            assertEquals(topic, outboxMessageEntitySlot.captured.topic)
            verify(exactly = 1) { outboxMessageRepository.save(outboxMessageEntitySlot.captured) }
        }
    }

    @Nested
    inner class GetMessages {

        @Test
        fun success() {

            outboxMessageRepository.save(homeDeletedMessage)
            outboxMessageRepository.save(roomDeletedMessage)

            val outboxMessages = outboxMessageService.getAllMessages()

            assertEquals(2, outboxMessages.size)
        }
    }

    @Nested
    inner class DeleteMessages {

        @Test
        fun success() {

            val savedFirst = outboxMessageRepository.save(homeDeletedMessage)
            val savedSecond = outboxMessageRepository.save(roomDeletedMessage)
            assertEquals(2, outboxMessageRepository.findAll().size)

            outboxMessageService.deleteMessages(listOf(savedFirst.id, savedSecond.id))

            assertEquals(0, outboxMessageRepository.findAll().size)
            verify(exactly = 1) { outboxMessageRepository.deleteAllByIdInBatch(listOf(savedFirst.id, savedSecond.id)) }
        }
    }
}

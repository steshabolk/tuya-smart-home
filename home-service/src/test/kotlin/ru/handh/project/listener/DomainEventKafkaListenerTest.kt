package ru.handh.project.listener

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import ru.handh.project.repository.HomeRepository
import ru.handh.project.repository.OutboxMessageRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.service.HomeService
import ru.handh.project.service.OutboxMessageService
import ru.handh.project.util.homeDeletedTopic
import ru.handh.project.util.homeEntity
import ru.handh.project.util.userDeletedEvent

@Import(DomainEventKafkaListener::class)
@DataJpaTest
@EnableConfigurationProperties(value = [KafkaTopicsProperties::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class DomainEventKafkaListenerTest {

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @SpykBean
    private lateinit var outboxMessageService: OutboxMessageService

    @SpykBean
    private lateinit var outboxMessageRepository: OutboxMessageRepository

    @SpykBean
    private lateinit var homeService: HomeService

    @SpykBean
    private lateinit var homeRepository: HomeRepository

    @Autowired
    private lateinit var kafkaListener: DomainEventKafkaListener

    @Nested
    inner class ListenDomainEvent {

        @Test
        fun listenUserDeletedEvent() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            assertEquals(0, outboxMessageRepository.findAll().size)

            kafkaListener.listen(userDeletedEvent)

            assertFalse(homeRepository.existsById(savedHome.id))
            val outboxMessages = outboxMessageRepository.findAll()
            assertEquals(1, outboxMessages.size)
            assertEquals(homeDeletedTopic, outboxMessages[0].topic)
        }
    }
}

package ru.handh.project.listener

import com.ninjasquad.springmockk.SpykBean
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.config.KafkaTopicsProperties
import ru.handh.project.connector.DeviceConnector
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.repository.DeviceRepository
import ru.handh.project.service.DeviceService
import ru.handh.project.util.DeviceConnectorConfig
import ru.handh.project.util.deviceEntity
import ru.handh.project.util.homeDeletedEvent
import ru.handh.project.util.roomDeletedEvent


@Import(DomainEventKafkaListener::class, DeviceConnectorConfig::class)
@DataJpaTest
@EnableConfigurationProperties(value = [KafkaTopicsProperties::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class DomainEventKafkaListenerTest {

    @MockBean
    private lateinit var deviceConnector: DeviceConnector

    @SpykBean
    private lateinit var deviceService: DeviceService

    @SpykBean
    private lateinit var deviceRepository: DeviceRepository

    @Autowired
    private lateinit var kafkaListener: DomainEventKafkaListener

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun listenHomeDeletedEvent() {

        val savedDevice = deviceRepository.save(deviceEntity)
        assertTrue(deviceRepository.existsById(savedDevice.id))

        kafkaListener.listen(homeDeletedEvent)

        assertFalse(deviceRepository.existsById(savedDevice.id))
    }

    @Test
    fun listenRoomDeletedEvent() {

        val savedDevice = deviceRepository.save(deviceEntity)
        assertTrue(deviceRepository.existsById(savedDevice.id))

        kafkaListener.listen(roomDeletedEvent)
        entityManager.refresh(savedDevice)

        deviceRepository.findByIdOrNull(savedDevice.id)
        assertTrue(deviceRepository.existsById(savedDevice.id))
        assertEquals(null, savedDevice.roomId)
    }
}

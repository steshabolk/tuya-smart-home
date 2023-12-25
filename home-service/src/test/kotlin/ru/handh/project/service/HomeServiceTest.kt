package ru.handh.project.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.entity.HomeEntity
import ru.handh.project.enum.DomainEventType
import ru.handh.project.exception.ApiError
import ru.handh.project.model.HomeDeletedEvent
import ru.handh.project.repository.HomeRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.expectApiError
import ru.handh.project.util.homeDeletedMessage
import ru.handh.project.util.homeDto
import ru.handh.project.util.homeEditRequest
import ru.handh.project.util.homeEntity
import ru.handh.project.util.homeEntityWithAnotherOwner
import ru.handh.project.util.homeNotFoundId
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.homeRequest

@Import(HomeService::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class HomeServiceTest {

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @MockkBean
    private lateinit var outboxMessageService: OutboxMessageService

    @SpykBean
    private lateinit var homeRepository: HomeRepository

    @Autowired
    private lateinit var homeService: HomeService

    private val homeEntitySlot = slot<HomeEntity>()
    private val homeDeletedEventSlot = slot<HomeDeletedEvent>()

    @Nested
    inner class CreateHome {

        @Test
        fun success() {

            every { homeRepository.save(capture(homeEntitySlot)) } answers { callOriginal() }

            val home = homeService.createHome(homeOwnerId, homeRequest)

            assertTrue(homeRepository.existsById(home.id))
            assertEquals(homeDto.name, home.name)
            assertEquals(homeDto.address, home.address)

            verify(exactly = 1) { homeRepository.save(homeEntitySlot.captured) }
            assertEquals(homeOwnerId, homeEntitySlot.captured.ownerId)
            assertEquals(homeRequest.name, homeEntitySlot.captured.name)
        }
    }

    @Nested
    inner class EditHome {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            val oldName = savedHome.name

            every { homeRepository.save(capture(homeEntitySlot)) } answers { callOriginal() }

            val editedHome = homeService.editHome(savedHome.id, savedHome.ownerId, homeEditRequest)

            assertEquals(savedHome.id, editedHome.id)
            assertNotEquals(oldName, editedHome.name)

            verify(exactly = 1) { homeRepository.findById(savedHome.id) }

            assertEquals(savedHome.id, homeEntitySlot.captured.id)
            assertEquals(savedHome.ownerId, homeEntitySlot.captured.ownerId)
            assertEquals(editedHome.name, homeEntitySlot.captured.name)
            verify(exactly = 1) { homeRepository.save(homeEntitySlot.captured) }
        }

        @Test
        fun `should throw an exception if home is not found`() {

            assertFalse(homeRepository.existsById(homeNotFoundId))


            expectApiError(ApiError.HOME_NOT_FOUND) {
                homeService.editHome(homeNotFoundId, homeOwnerId, homeEditRequest)
            }
        }

        @Test
        fun `should throw an exception if home owner does not match`() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            assertNotEquals(2, savedHome.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                homeService.editHome(savedHome.id, 2, homeEditRequest)
            }

            verify(exactly = 1) { homeRepository.findById(savedHome.id) }
        }
    }

    @Nested
    inner class GetHome {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))

            val home = homeService.getHome(savedHome.id, savedHome.ownerId)

            assertEquals(savedHome.id, home.id)
            verify(exactly = 1) { homeRepository.findWithRoomsById(savedHome.id) }
        }

        @Test
        fun `should throw an exception if home is not found`() {

            assertFalse(homeRepository.existsById(homeNotFoundId))

            expectApiError(ApiError.HOME_NOT_FOUND) {
                homeService.getHome(homeNotFoundId, homeOwnerId)
            }
        }

        @Test
        fun `should throw an exception if home owner does not match`() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            assertNotEquals(2, savedHome.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                homeService.getHome(savedHome.id, 2)
            }

            verify(exactly = 1) { homeRepository.findWithRoomsById(savedHome.id) }
        }
    }

    @Nested
    inner class GetHomes {

        @Test
        fun success() {

            val savedFirst = homeRepository.save(homeEntity)
            val savedSecond = homeRepository.save(homeEntityWithAnotherOwner)
            assertEquals(2, homeRepository.findAll().size)
            assertNotEquals(savedFirst.ownerId, savedSecond.ownerId)

            val homes = homeService.getHomes(savedFirst.ownerId)

            assertEquals(1, homes.size)
            assertEquals(savedFirst.id, homes[0].id)
        }
    }

    @Nested
    inner class DeleteHome {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))

            every { outboxMessageService.saveMessage(capture(homeDeletedEventSlot)) } returns homeDeletedMessage

            homeService.deleteHome(savedHome.id)

            assertFalse(homeRepository.existsById(savedHome.id))

            assertEquals(DomainEventType.HOME_DELETED, homeDeletedEventSlot.captured.type)
            assertEquals(savedHome.id, homeDeletedEventSlot.captured.message.id)
            verify(exactly = 1) { outboxMessageService.saveMessage(homeDeletedEventSlot.captured) }

            verify(exactly = 1) { homeRepository.findById(savedHome.id) }
            verify(exactly = 1) { homeRepository.deleteById(savedHome.id) }
        }

        @Test
        fun `should throw an exception if home is not found`() {

            assertFalse(homeRepository.existsById(homeNotFoundId))

            expectApiError(ApiError.HOME_NOT_FOUND) {
                homeService.deleteHome(homeNotFoundId)
            }

            verify(exactly = 1) { homeRepository.findById(homeNotFoundId) }
            verify(exactly = 0) { homeRepository.deleteById(homeNotFoundId) }
        }
    }

    @Nested
    inner class DeleteHomeByOwnerId {

        @Test
        fun success() {

            val savedFirst = homeRepository.save(homeEntity)
            val savedSecond = homeRepository.save(homeEntityWithAnotherOwner)
            assertEquals(2, homeRepository.findAll().size)
            assertNotEquals(savedFirst.ownerId, savedSecond.ownerId)

            every { outboxMessageService.saveMessage(capture(homeDeletedEventSlot)) } returns homeDeletedMessage

            homeService.deleteByOwnerId(savedFirst.ownerId)

            assertEquals(1, homeRepository.findAll().size)
            assertFalse(homeRepository.existsById(savedFirst.id))

            verify(exactly = 1) { homeRepository.findAllByOwnerId(savedFirst.ownerId) }

            assertEquals(DomainEventType.HOME_DELETED, homeDeletedEventSlot.captured.type)
            assertEquals(savedFirst.id, homeDeletedEventSlot.captured.message.id)
            verify(exactly = 1) { outboxMessageService.saveMessage(homeDeletedEventSlot.captured) }
        }
    }
}

package ru.handh.project.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.container.PostgresTestContainer
import ru.handh.project.entity.RoomEntity
import ru.handh.project.enum.DomainEventType
import ru.handh.project.exception.ApiError
import ru.handh.project.model.RoomDeletedEvent
import ru.handh.project.repository.HomeRepository
import ru.handh.project.repository.RoomRepository
import ru.handh.project.scheduler.ScheduledTask
import ru.handh.project.util.expectApiError
import ru.handh.project.util.homeEntity
import ru.handh.project.util.homeId
import ru.handh.project.util.homeOwnerId
import ru.handh.project.util.roomDeletedMessage
import ru.handh.project.util.roomDto
import ru.handh.project.util.roomEditRequest
import ru.handh.project.util.roomId
import ru.handh.project.util.roomRequest

@Import(RoomService::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(
    classes = [],
    initializers = [PostgresTestContainer.Initializer::class]
)
class RoomServiceTest {

    @MockkBean
    private lateinit var scheduledTask: ScheduledTask

    @MockkBean
    private lateinit var outboxMessageService: OutboxMessageService

    @SpykBean
    private lateinit var homeRepository: HomeRepository

    @SpykBean
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var entityManager: EntityManager

    private val roomEntitySlot = slot<RoomEntity>()
    private val roomDeletedEventSlot = slot<RoomDeletedEvent>()

    @Nested
    inner class CreateRoom {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))

            every { roomRepository.save(capture(roomEntitySlot)) } answers { callOriginal() }

            val room = roomService.createRoom(savedHome.id, savedHome.ownerId, roomRequest)
            entityManager.refresh(savedHome)

            assertTrue(roomRepository.existsById(room.id))
            assertEquals(roomDto.name, room.name)
            assertEquals(1, savedHome.rooms?.size)
            assertEquals(room.id, savedHome.rooms?.get(0)?.id)

            verify(exactly = 1) { roomRepository.save(roomEntitySlot.captured) }
        }

        @Test
        fun `should throw an exception if home is not found`() {

            assertFalse(homeRepository.existsById(homeId))

            expectApiError(ApiError.HOME_NOT_FOUND) {
                roomService.createRoom(homeId, homeOwnerId, roomRequest)
            }
        }

        @Test
        fun `should throw an exception if home owner does not match`() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            assertNotEquals(2, savedHome.ownerId)

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                roomService.createRoom(savedHome.id, 2, roomRequest)
            }

        }
    }

    @Nested
    inner class EditRoom {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))

            val savedRoom = roomService.createRoom(savedHome.id, savedHome.ownerId, roomRequest)
            assertTrue(roomRepository.existsById(savedRoom.id))

            every { roomRepository.save(capture(roomEntitySlot)) } answers { callOriginal() }

            val editedRoom = roomService.editRoom(savedRoom.id, savedHome.ownerId, roomEditRequest)

            assertEquals(savedRoom.id, editedRoom.id)
            assertNotEquals(savedRoom.name, editedRoom.name)

            entityManager.refresh(savedHome)
            assertEquals(1, savedHome.rooms?.size)
            assertEquals(editedRoom.id, savedHome.rooms?.get(0)?.id)

            verify(exactly = 1) { roomRepository.save(roomEntitySlot.captured) }
        }

        @Test
        fun `should throw an exception if room is not found`() {

            assertFalse(roomRepository.existsById(roomId))

            expectApiError(ApiError.ROOM_NOT_FOUND) {
                roomService.editRoom(roomId, homeOwnerId, roomEditRequest)
            }
        }

        @Test
        fun `should throw an exception if home owner does not match`() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))
            assertNotEquals(2, savedHome.ownerId)

            val savedRoom = roomService.createRoom(savedHome.id, savedHome.ownerId, roomRequest)
            assertTrue(roomRepository.existsById(savedRoom.id))

            expectApiError(ApiError.OWNER_NOT_MATCH) {
                roomService.editRoom(savedRoom.id, 2, roomEditRequest)
            }
        }
    }

    @Nested
    inner class DeleteRoom {

        @Test
        fun success() {

            val savedHome = homeRepository.save(homeEntity)
            assertTrue(homeRepository.existsById(savedHome.id))

            val savedRoom = roomService.createRoom(savedHome.id, savedHome.ownerId, roomRequest)
            assertTrue(roomRepository.existsById(savedRoom.id))

            every { outboxMessageService.saveMessage(capture(roomDeletedEventSlot)) } returns roomDeletedMessage

            roomService.deleteRoom(savedRoom.id)

            assertFalse(roomRepository.existsById(savedRoom.id))
            assertEquals(DomainEventType.ROOM_DELETED, roomDeletedEventSlot.captured.type)
            assertEquals(savedRoom.id, roomDeletedEventSlot.captured.message.id)
            verify(exactly = 1) { outboxMessageService.saveMessage(roomDeletedEventSlot.captured) }

            verify(exactly = 1) { roomRepository.deleteById(savedRoom.id) }
        }

        @Test
        fun `should throw an exception if room is not found`() {

            assertFalse(roomRepository.existsById(roomId))

            expectApiError(ApiError.ROOM_NOT_FOUND) {
                roomService.deleteRoom(roomId)
            }
        }
    }
}

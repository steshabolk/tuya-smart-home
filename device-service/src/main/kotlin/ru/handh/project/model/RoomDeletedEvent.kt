package ru.handh.project.model

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.enum.DomainEventType
import ru.handh.project.util.ROOM_DELETED

@JsonTypeName(ROOM_DELETED)
data class RoomDeletedEvent(
    override val message: DeletedMessageDto
) : DomainEvent(type = DomainEventType.ROOM_DELETED)

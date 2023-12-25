package ru.handh.project.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.handh.project.enum.DomainEventType
import ru.handh.project.util.HOME_DELETED
import ru.handh.project.util.ROOM_DELETED

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HomeDeletedEvent::class, name = HOME_DELETED),
    JsonSubTypes.Type(value = RoomDeletedEvent::class, name = ROOM_DELETED),
)
sealed class DomainEvent(
    val type: DomainEventType,
) {
    abstract val message: Any
}

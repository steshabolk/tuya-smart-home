package ru.handh.project.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.handh.project.enum.DomainEventType

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
sealed class DomainEvent(
    val type: DomainEventType,
) {
    abstract val message: Any
}

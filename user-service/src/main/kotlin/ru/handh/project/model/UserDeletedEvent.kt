package ru.handh.project.model

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.enum.DomainEventType

@JsonTypeName("USER_DELETED")
data class UserDeletedEvent(
    override val message: DeletedMessageDto
) : DomainEvent(type = DomainEventType.USER_DELETED)

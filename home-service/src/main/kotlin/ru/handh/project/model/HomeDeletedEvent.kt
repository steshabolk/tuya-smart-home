package ru.handh.project.model

import com.fasterxml.jackson.annotation.JsonTypeName
import ru.handh.project.dto.kafka.DeletedMessageDto
import ru.handh.project.enum.DomainEventType
import ru.handh.project.util.HOME_DELETED

@JsonTypeName(HOME_DELETED)
data class HomeDeletedEvent(
    override val message: DeletedMessageDto
) : DomainEvent(type = DomainEventType.HOME_DELETED)

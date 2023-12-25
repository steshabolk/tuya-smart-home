package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.handh.project.entity.OutboxMessageEntity

interface OutboxMessageRepository : JpaRepository<OutboxMessageEntity, Int> {
}

package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.handh.project.entity.TelegramTokenEntity

interface TelegramTokenRepository : JpaRepository<TelegramTokenEntity, Int> {

    fun findByUserId(userId: Int): TelegramTokenEntity?

    fun findByToken(token: String): TelegramTokenEntity?
}

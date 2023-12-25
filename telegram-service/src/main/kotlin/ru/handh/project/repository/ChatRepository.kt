package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.handh.project.entity.ChatEntity

@Repository
interface ChatRepository : JpaRepository<ChatEntity, Int> {

    fun findByChatId(chatId: Long): ChatEntity?

    fun existsByChatId(chatId: Long): Boolean

    fun deleteByChatId(chatId: Long)
}

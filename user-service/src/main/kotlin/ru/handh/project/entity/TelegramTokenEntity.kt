package ru.handh.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(name = "telegram_tokens")
data class TelegramTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int = -1,

    @field:NotNull
    @Column(name = "user_id")
    val userId: Int,

    @field:NotNull
    @field:Size(min = 36, max = 36)
    @Column(name = "token")
    val token: String,
)

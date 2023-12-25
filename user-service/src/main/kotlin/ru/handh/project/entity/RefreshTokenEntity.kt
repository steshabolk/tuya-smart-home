package ru.handh.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
data class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int = -1,

    @field:NotNull
    @field:Size(min = 36, max = 36)
    @Column(name = "jwt_id")
    val jwtID: String,

    @field:NotNull
    @Column(name = "expires_at")
    val expiresAt: Instant
)

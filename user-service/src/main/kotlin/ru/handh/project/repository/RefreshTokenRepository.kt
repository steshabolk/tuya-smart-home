package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.handh.project.entity.RefreshTokenEntity
import java.time.Instant

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Int> {

    fun findByJwtID(refreshToken: String): RefreshTokenEntity?

    fun deleteAllByExpiresAtLessThanEqual(expiresAt: Instant)

    fun deleteByJwtID(refreshToken: String)
}

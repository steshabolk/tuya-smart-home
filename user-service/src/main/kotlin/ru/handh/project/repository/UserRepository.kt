package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.handh.project.entity.UserEntity

@Repository
interface UserRepository : JpaRepository<UserEntity, Int> {

    fun existsByUsername(username: String) : Boolean

    fun findByUsername(username: String) : UserEntity?
}

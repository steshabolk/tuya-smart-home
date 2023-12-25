package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.handh.project.entity.RoomEntity

@Repository
interface RoomRepository : JpaRepository<RoomEntity, Int> {
}

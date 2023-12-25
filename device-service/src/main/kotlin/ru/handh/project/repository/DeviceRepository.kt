package ru.handh.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.handh.project.entity.DeviceEntity

@Repository
interface DeviceRepository : JpaRepository<DeviceEntity, Int> {

    fun findAllByHomeIdAndRoomId(homeId: Int, roomId: Int) : List<DeviceEntity>

    fun findAllByHomeId(homeId: Int) : List<DeviceEntity>

    fun existsByTuyaDeviceId(tuyaDeviceId: String) : Boolean

    fun deleteAllByHomeId(homeId: Int)

    @Modifying
    @Query("UPDATE DeviceEntity d SET d.roomId = null WHERE d.roomId = :roomId")
    fun resetRoom(@Param("roomId") roomId: Int)
}

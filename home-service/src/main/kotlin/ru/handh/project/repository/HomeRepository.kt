package ru.handh.project.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import ru.handh.project.entity.HomeEntity

@Repository
interface HomeRepository : JpaRepository<HomeEntity, Int> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = ["rooms"])
    fun findWithRoomsById(id: Int) = findByIdOrNull(id)

    fun findAllByOwnerId(ownerId: Int) : List<HomeEntity>
}

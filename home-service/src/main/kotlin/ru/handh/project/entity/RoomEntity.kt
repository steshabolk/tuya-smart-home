package ru.handh.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.handh.project.dto.RoomDto

@Entity
@Table(name = "rooms")
data class RoomEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int,

    @field:NotNull
    @field:Size(min = 2, max = 64)
    @Column(name = "name")
    val name: String,

    @field:NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "home_id", referencedColumnName = "id")
    val home: HomeEntity,
) {

    fun toDto() =
        RoomDto(
            id = id,
            name = name
        )
}

package ru.handh.project.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.handh.project.dto.HomeDto
import ru.handh.project.dto.SimpleHomeDto
import ru.handh.project.exception.ApiError

@Entity
@Table(name = "homes")
data class HomeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int,

    @field:NotNull
    @field:Size(min = 2, max = 64)
    @Column(name = "name")
    val name: String,

    @field:Size(min = 2, max = 128)
    @Column(name = "address")
    val address: String?,

    @OneToMany(mappedBy = "home", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val rooms: List<RoomEntity>? = null,

    @field:NotNull
    @Column(name = "owner_id")
    val ownerId: Int
) {

    fun toDto() =
        HomeDto(
            id = id,
            name = name,
            address = address,
            rooms = rooms?.map { it.toDto() } ?: emptyList()
        )

    fun toSimpleDto() =
        SimpleHomeDto(
            id = id,
            name = name
        )

    fun checkOwner(ownerId: Int) =
        if (this.ownerId != ownerId) throw ApiError.OWNER_NOT_MATCH.toException(ownerId, id)
        else this
}

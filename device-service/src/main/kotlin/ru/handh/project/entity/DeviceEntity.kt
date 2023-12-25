package ru.handh.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.handh.project.dto.CommandDto
import ru.handh.project.dto.DeviceDto
import ru.handh.project.dto.SimpleDeviceDto
import ru.handh.project.enum.DeviceCategory

@Entity
@Table(name = "devices")
data class DeviceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int,

    @field:NotBlank
    @Column(name = "tuya_device_id")
    val tuyaDeviceId: String,

    @field:NotNull
    @Column(name = "owner_id")
    val ownerId: Int,

    @field:NotNull
    @Column(name = "home_id")
    val homeId: Int,

    @Column(name = "room_id")
    val roomId: Int?,

    @field:NotBlank
    @field:Size(min = 2, max = 64)
    @Column(name = "name")
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    val category: DeviceCategory,

    @Transient
    var capabilities: List<CommandDto>? = null
) {

    fun toDto() =
        DeviceDto(
            id = id,
            name = name,
            category = category.name,
            capabilities = capabilities ?: emptyList()
        )

    fun toSimpleDto() =
        SimpleDeviceDto(
            id = id,
            name = name,
            category = category.name,
        )
}

package ru.handh.project.dto

class HomeDto(
    val id: Int,
    val name: String,
    val address: String?,
    val rooms: List<RoomDto>
)

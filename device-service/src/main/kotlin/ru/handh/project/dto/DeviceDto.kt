package ru.handh.project.dto

class DeviceDto (
    val id: Int,
    val name: String,
    val category: String,
    val capabilities: List<CommandDto>
)

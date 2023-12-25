package ru.handh.project.dto

import ru.handh.project.enum.DeviceCommand


class DeviceCommandDto(
    val messageId: Int,
    val deviceCommand: DeviceCommand
)

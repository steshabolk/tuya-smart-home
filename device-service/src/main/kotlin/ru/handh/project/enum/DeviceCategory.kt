package ru.handh.project.enum

import ru.handh.project.exception.ApiError
import ru.handh.project.util.LIGHT as LIGHT_VALUE

enum class DeviceCategory(
    private val tuyaCode: String
) {
    LIGHT(LIGHT_VALUE);

    companion object {
        fun from(s: String): DeviceCategory = values().find { it.tuyaCode == s }
            ?: throw ApiError.UNKNOWN_DEVICE_CATEGORY.toException(s)
    }
}

package ru.handh.project.enum

import ru.handh.device.client.model.ExceptionResponseGen as DeviceException
import ru.handh.home.client.model.ExceptionResponseGen as HomeException
import ru.handh.user.client.model.ExceptionResponseGen as UserException

enum class ClientType(
    val errorClass: Class<*>
) {
    USER(UserException::class.java),
    HOME(HomeException::class.java),
    DEVICE(DeviceException::class.java),
    NON(String::class.java)
}

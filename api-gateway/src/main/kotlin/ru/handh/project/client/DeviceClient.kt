package ru.handh.project.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.handh.device.client.api.DeviceApi

@Component
class DeviceClient(
    @Value(value = "\${api.device.url}")
    private val path: String
) : DeviceApi(
    basePath = path,
    restTemplate = RestTemplate()
)

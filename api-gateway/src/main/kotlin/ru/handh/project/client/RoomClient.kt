package ru.handh.project.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.handh.home.client.api.HomeApi
import ru.handh.home.client.api.RoomApi

@Component
class RoomClient(
    @Value(value = "\${api.home.url}")
    private val path: String
) : RoomApi(
    basePath = path,
    restTemplate = RestTemplate()
)

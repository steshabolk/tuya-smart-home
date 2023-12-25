package ru.handh.project.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.handh.user.client.api.UserApi

@Component
class UserClient(
    @Value(value = "\${api.user.url}")
    private val path: String
) : UserApi(
    basePath = path,
    restTemplate = RestTemplate()
)

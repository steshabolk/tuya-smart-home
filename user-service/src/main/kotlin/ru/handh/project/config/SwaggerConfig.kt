package ru.handh.project.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info =
    Info(
        title = "user-service",
        description = "API for users and tokens management",
        version = "v1"
    ),
    tags = [
        Tag(name = "User")
    ]
)
@Configuration
class SwaggerConfig

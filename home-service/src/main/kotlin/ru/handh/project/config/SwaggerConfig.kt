package ru.handh.project.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info =
    Info(
        title = "home-service",
        description = "API for homes and rooms management",
        version = "v1"
    ),
    tags = [
        Tag(name = "Home"),
        Tag(name = "Room")
    ]
)
@Configuration
class SwaggerConfig

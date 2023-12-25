package ru.handh.project.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info =
    Info(
        title = "device-service",
        description = "API for devices management",
        version = "v1"
    ),
    tags = [
        Tag(name = "Device")
    ]
)
@Configuration
class SwaggerConfig

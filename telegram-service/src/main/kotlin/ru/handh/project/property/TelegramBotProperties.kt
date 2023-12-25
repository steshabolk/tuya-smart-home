package ru.handh.project.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram-bot.config")
class TelegramBotProperties(
    val name: String,
    val token: String
)

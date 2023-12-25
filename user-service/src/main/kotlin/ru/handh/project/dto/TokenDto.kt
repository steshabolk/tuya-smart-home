package ru.handh.project.dto

class TokenDto(
    val accessToken: String,
    val refreshToken: String,
    val ttl: Int
)

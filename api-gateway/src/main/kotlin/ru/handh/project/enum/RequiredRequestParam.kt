package ru.handh.project.enum

enum class RequiredRequestParam(
    val attributeName: String
) {
    USER_ID("userId"),
    JWT_ID("jwtId")
}

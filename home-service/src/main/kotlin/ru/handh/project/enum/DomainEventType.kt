package ru.handh.project.enum

enum class DomainEventType(
    val topicPropName: String
) {
    HOME_DELETED("home-deleted"),
    ROOM_DELETED("room-deleted"),
    USER_DELETED("user-deleted")
}

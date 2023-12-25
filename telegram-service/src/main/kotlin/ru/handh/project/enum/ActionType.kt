package ru.handh.project.enum

enum class ActionType(
    val command: String,
    val description: String
) {
    START("/start", "welcome message"),
    ACCOUNT("/account", "account"),
    LOGIN("/login", "login"),
    TOKEN_AUTH("/auth", "auth"),
    LOGOUT("/logout", "logout"),
    CONTROL("/control", "devices control"),
    HOME("/home", "home"),
    DEVICE("/device", "device"),
    SWITCH_LED("/switch-led", "switch-led"),
    TEMPERATURE("/temperature", "temperature"),
    COLOR("/color", "color"),
    BRIGHTNESS("/brightness", "brightness"),
    EDIT_STATUS("/edit-status", "edit status");
}

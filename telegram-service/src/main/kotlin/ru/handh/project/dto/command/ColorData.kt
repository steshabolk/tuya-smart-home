package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.handh.project.dto.action.ActionData
import ru.handh.project.enum.ActionType

class ColorData(
    chatId: Long,
    messageId: Int,
    val messageText: String
) : ActionData(chatId, messageId, ActionType.COLOR) {

    class ColorHSV(
        @JsonProperty("h")
        val hue: Int?,

        @JsonProperty("s")
        val saturation: Int?,

        @JsonProperty("v")
        val value: Int?
    ) {
        @JsonCreator
        constructor(json: String) : this(
            jacksonObjectMapper().readTree(json)["h"]?.asInt(),
            jacksonObjectMapper().readTree(json)["s"]?.asInt(),
            jacksonObjectMapper().readTree(json)["v"]?.asInt()
        )
    }
}

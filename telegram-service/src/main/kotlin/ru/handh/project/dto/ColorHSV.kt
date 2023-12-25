package ru.handh.project.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

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

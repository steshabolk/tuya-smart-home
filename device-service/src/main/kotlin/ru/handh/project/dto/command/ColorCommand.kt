package ru.handh.project.dto.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import ru.handh.project.enum.CommandCode
import ru.handh.project.util.COLOR
import ru.handh.project.validator.Range

@JsonTypeName(COLOR)
class ColorCommand(
    code: CommandCode,
    @field:Valid
    override val value: ColorHSV
) : Command(code) {

    class ColorHSV(
        @JsonProperty("h")
        @get:NotNull
        @field:Range(min = 0, max = 360, field = "hue")
        val hue: Int?,

        @JsonProperty("s")
        @get:NotNull
        @field:Range(min = 0, max = 1000, field = "saturation")
        val saturation: Int?,

        @JsonProperty("v")
        @get:NotNull
        @field:Range(min = 0, max = 1000, field = "value")
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

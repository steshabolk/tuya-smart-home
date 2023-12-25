package ru.handh.project.util

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.util.LinkedMultiValueMap
import ru.handh.project.exception.ApiError
import ru.handh.project.exception.ApiException
import ru.handh.project.exception.ExceptionResponse
import java.nio.charset.StandardCharsets

private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

fun MockMvc.doPostRequest(request: Any, path: String, params: Map<String, String>) =
    this.perform(
        MockMvcRequestBuilders.post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsBytes(request))
            .params( LinkedMultiValueMap<String, String>().apply { setAll(params) })
    )

fun MockMvc.doPutRequest(request: Any, path: String, params: Map<String, String>) =
    this.perform(
        MockMvcRequestBuilders.put(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsBytes(request))
            .params( LinkedMultiValueMap<String, String>().apply { setAll(params) })
    )

fun MockMvc.doGetRequest(path: String, params: Map<String, String>) =
    this.perform(
        MockMvcRequestBuilders.get(path)
            .params( LinkedMultiValueMap<String, String>().apply { setAll(params) })
    )

fun MockMvc.doDeleteRequest(path: String, params: Map<String, String>) =
    this.perform(
        MockMvcRequestBuilders.delete(path)
            .params( LinkedMultiValueMap<String, String>().apply { setAll(params) })
    )

fun ResultActions.expectJson(expected: Any) {
    assertEquals(
        mapper.writeValueAsString(expected),
        andReturn().response.getContentAsString(StandardCharsets.UTF_8)
    )
}

fun ResultActions.expectEmptyResponse() {
    assertEquals(
        0,
        andReturn().response.contentLength
    )
}

fun ResultActions.expectOkStatus() {
    this.andExpect(MockMvcResultMatchers.status().isOk)
}

fun ResultActions.expectValidationError() {
    val ex = getExceptionResponse()
    assertEquals("VALIDATION_ERROR", ex.code)
    this.andExpect(MockMvcResultMatchers.status().isBadRequest)
}

fun ResultActions.expectInternalApiError() {
    val ex = getExceptionResponse()
    assertEquals("INTERNAL_API_ERROR", ex.code)
    this.andExpect(MockMvcResultMatchers.status().isBadRequest)
}

fun expectApiError(apiError: ApiError, executable: () -> Unit) {
    val ex = assertThrows<ApiException> { executable() }
    assertEquals(apiError.name, ex.code)
}

private fun ResultActions.getExceptionResponse(): ExceptionResponse {
    val contentAsString = andReturn().response.getContentAsString(StandardCharsets.UTF_8)
    return mapper.readValue(contentAsString, ExceptionResponse::class.java)
}

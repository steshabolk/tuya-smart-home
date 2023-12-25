package ru.handh.project.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import ru.handh.project.exception.ApiError
import ru.handh.project.exception.ApiException

fun expectApiError(apiError: ApiError, executable: () -> Unit) {
    val ex = assertThrows<ApiException> { executable() }
    assertEquals(apiError.name, ex.code)
}

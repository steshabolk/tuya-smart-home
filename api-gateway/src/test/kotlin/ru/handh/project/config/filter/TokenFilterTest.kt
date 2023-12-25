package ru.handh.project.config.filter

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.annotation.DirtiesContext
import ru.handh.project.config.GlobalExceptionHandler
import ru.handh.project.enum.RequiredRequestParam
import ru.handh.project.exception.ApiError
import ru.handh.project.exception.ApiException
import ru.handh.project.service.GatewayService
import ru.handh.project.service.EncryptionService
import ru.handh.project.service.SignatureService
import ru.handh.project.service.TokenService
import ru.handh.project.util.JWKServiceMock
import ru.handh.project.util.accessToken
import ru.handh.project.util.accessTtl
import ru.handh.project.util.auth
import ru.handh.project.util.refresh
import ru.handh.project.util.refreshToken
import ru.handh.project.util.register
import ru.handh.project.util.signout
import ru.handh.project.util.telegram
import ru.handh.project.util.tokenHeader
import ru.handh.project.util.userId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TokenFilterTest {

    @SpykBean
    private lateinit var exceptionHandler: GlobalExceptionHandler

    @SpykBean
    private lateinit var request: MockHttpServletRequest

    @SpykBean
    private lateinit var response: MockHttpServletResponse

    @SpykBean
    private lateinit var filterChain: MockFilterChain

    @MockkBean
    private lateinit var encryptionService: EncryptionService

    @MockkBean
    private lateinit var signatureService: SignatureService

    @SpykBean
    private lateinit var tokenFilter: TokenFilter

    @SpykBean
    private lateinit var tokenService: TokenService

    @SpykBean
    private lateinit var gatewayService: GatewayService

    @SpykBean
    private lateinit var jwkService: JWKServiceMock

    private val apiExceptionSlot = slot<ApiException>()

    @BeforeEach
    fun setUp() {
        every { filterChain.doFilter(any(), any()) } just Runs
    }

    @ParameterizedTest
    @CsvSource("$register,POST", "$auth,POST", "$telegram,POST")
    fun `request does not require a token`(path: String, method: String) {

        every { request.servletPath } answers { path }
        every { request.method } answers { method }

        tokenFilter.doFilter(request, response, filterChain)

        verify(exactly = 1) { gatewayService.containsPath(path, method, any()) }
        verify(exactly = 0) { request.getHeader(tokenHeader) }
    }

    @Test
    fun `response should have status code 503 if token service is not available`() {

        every { request.servletPath } answers { refresh }
        every { request.method } answers { "POST" }
        every { tokenService.checkTokenServiceAvailability() } answers { false }

        tokenFilter.doFilter(request, response, filterChain)

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.status)
        verify(exactly = 1) { exceptionHandler.handleApiResponseStatus(any()) }
    }

    @Test
    fun `should throw an exception if required token header is missing`() {

        every { request.servletPath } answers { refresh }
        every { request.method } answers { "POST" }
        every { tokenService.checkTokenServiceAvailability() } answers { true }
        every { exceptionHandler.handleApiException(capture(apiExceptionSlot)) } answers { callOriginal() }

        tokenFilter.doFilter(request, response, filterChain)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals(ApiError.MISSING_ACCESS_TOKEN.name, apiExceptionSlot.captured.code)
        verify(exactly = 1) { exceptionHandler.handleApiException(any()) }
    }

    @Test
    fun `should throw an exception if token is expired`() {

        every { request.servletPath } answers { signout }
        every { request.method } answers { "POST" }
        every { request.getHeader(tokenHeader) } answers { accessToken }
        every { tokenService.checkTokenServiceAvailability() } answers { true }
        every { exceptionHandler.handleApiException(capture(apiExceptionSlot)) } answers { callOriginal() }
        every { tokenService.getDecodedToken(accessToken) } answers {
            jwkService.generateDecodedToken(
                Date.from(
                    Instant.now().minus(
                        accessTtl.toLong(), ChronoUnit.MINUTES
                    )
                )
            )
        }

        tokenFilter.doFilter(request, response, filterChain)

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status)
        assertEquals(ApiError.TOKEN_EXPIRED.name, apiExceptionSlot.captured.code)
        verify(exactly = 1) { exceptionHandler.handleApiException(any()) }
    }

    @Test
    fun `set the userId and JwtId as the request attribute`() {

        every { request.servletPath } answers { refresh }
        every { request.method } answers { "POST" }
        every { request.getHeader(tokenHeader) } answers { accessToken }
        every { tokenService.checkTokenServiceAvailability() } answers { true }
        every { tokenService.getDecodedToken(accessToken) } answers {
            jwkService.generateDecodedToken(
                Date.from(
                    Instant.now().plus(
                        accessTtl.toLong(), ChronoUnit.MINUTES
                    )
                )
            )
        }

        tokenFilter.doFilter(request, response, filterChain)

        assertEquals(userId, request.getAttribute(RequiredRequestParam.USER_ID.attributeName))
        assertEquals(refreshToken, request.getAttribute(RequiredRequestParam.JWT_ID.attributeName))

        verify(exactly = 1) { tokenService.getUserIdFromPayload(any()) }
        verify(exactly = 1) { tokenService.getJwtIdFromPayload(any()) }
        verify(exactly = 1) { request.setAttribute(RequiredRequestParam.USER_ID.attributeName, any()) }
        verify(exactly = 1) { request.setAttribute(RequiredRequestParam.JWT_ID.attributeName, any()) }
    }
}

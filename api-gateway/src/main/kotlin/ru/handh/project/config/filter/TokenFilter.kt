package ru.handh.project.config.filter

import com.nimbusds.jwt.JWT
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import ru.handh.project.enum.RequiredRequestParam
import ru.handh.project.exception.ApiError
import ru.handh.project.exception.ApiResponseStatus
import ru.handh.project.service.GatewayService
import ru.handh.project.service.TokenService

private val log = KotlinLogging.logger {}

@Component
class TokenFilter(
        @Value(value = "\${jwt.header}")
    private val tokenHeader: String,

        private val gatewayService: GatewayService,
        private val tokenService: TokenService,
        private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath
        val method = request.method
        log.debug { "request: $method $path" }
        if (gatewayService.containsPath(path, method, gatewayService.securedPath)) {
            try {
                if (tokenService.checkTokenServiceAvailability()) {
                    val token = getTokenFromHeader(request, path, method)
                    if (gatewayService.containsPath(path, method, gatewayService.withUserIdParam)) {
                        tokenService.getUserIdFromPayload(token)
                            ?.run {
                                log.debug { "user id from decoded access token: $this" }
                                request.setAttribute(RequiredRequestParam.USER_ID.attributeName, this)
                            }
                    }
                    if (gatewayService.containsPath(path, method, gatewayService.withJwtIdParam)) {
                        tokenService.getJwtIdFromPayload(token)
                            ?.run { request.setAttribute(RequiredRequestParam.JWT_ID.attributeName, this) }
                    }
                } else {
                    throw ApiResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
                }
            } catch (ex: Exception) {
                handlerExceptionResolver.resolveException(request, response, null, ex)
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun getTokenFromHeader(request: HttpServletRequest, path: String, method:String): JWT {
        val header: String? = request.getHeader(tokenHeader)
        if (header.isNullOrBlank()) throw ApiError.MISSING_ACCESS_TOKEN.toException()
        val decoded = tokenService.getDecodedToken(header)
        if (gatewayService.containsPath(path, method, gatewayService.checkTokenExpirationPath)
            && tokenService.isExpired(decoded)) throw ApiError.TOKEN_EXPIRED.toException()
        return decoded
    }
}

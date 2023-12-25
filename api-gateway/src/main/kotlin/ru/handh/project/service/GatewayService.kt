package ru.handh.project.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ru.handh.project.enum.ClientType
import ru.handh.project.enum.RequestMethod
import ru.handh.project.enum.RequestMethod.Companion.allMethods
import ru.handh.project.enum.RequestMethod.Companion.from
import ru.handh.project.enum.RequiredRequestParam
import ru.handh.project.exception.ApiError

@Service
class GatewayService(
    @Value(value = "#{'\${api.home.path}'.split(',')}")
    private val homePath: List<String>,
    @Value(value = "#{'\${api.device.path}'.split(',')}")
    private val devicePath: List<String>,
    @Value(value = "#{'\${api.user.path}'.split(',')}")
    private val userPath: List<String>
) {

    final val withUserIdParam =
        devicePath.toMap(allMethods) + homePath.toMap(allMethods) +
                mapOf(
                    "/api/refresh" to listOf(RequestMethod.POST),
                    "/api/account" to listOf(RequestMethod.DELETE),
                    "/api/telegram" to listOf(RequestMethod.GET)
                )

    final
    val withJwtIdParam = mapOf(
        "/api/refresh" to listOf(RequestMethod.POST),
        "/api/signout" to listOf(RequestMethod.POST),
    )

    final
    val checkTokenExpirationPath =
        devicePath.toMap(allMethods) + homePath.toMap(allMethods) +
                mapOf(
                    "/api/signout" to listOf(RequestMethod.POST),
                    "/api/account" to listOf(RequestMethod.DELETE),
                )

    final val securedPath = withUserIdParam + withJwtIdParam

    fun getUserId() =
        getAttributeByName(RequiredRequestParam.USER_ID) as Int

    fun getJwtId() =
        getAttributeByName(RequiredRequestParam.JWT_ID) as String

    fun checkJwtIdsMatch(refreshToken: String) =
        when (getJwtId() == refreshToken) {
            true -> refreshToken
            false -> throw ApiError.TOKENS_NOT_MATCH.toException()
        }

    fun getClientTypeByServletPath(servletPath: String) =
        when {
            containsPath(servletPath, homePath) -> ClientType.HOME
            containsPath(servletPath, devicePath) -> ClientType.DEVICE
            containsPath(servletPath, userPath) -> ClientType.USER
            else -> ClientType.NON
        }

    fun containsPath(path: String, method: String, matchPaths: Map<String, List<RequestMethod>>): Boolean {
        val methods = matchPaths.keys.find { path.startsWith(it) }
            ?.let { matchPaths[it] }
            ?: return false
        return methods.contains(from(method))
    }

    private fun containsPath(path: String, matchPaths: List<String>) =
        matchPaths.any { path.startsWith(it) }

    private fun getAttributeByName(type: RequiredRequestParam) =
        (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            .request.getAttribute(type.attributeName)
            ?: throw ApiError.MISSING_REQUEST_ATTRIBUTE.toException(type.attributeName)

    private fun List<String>.toMap(method: List<RequestMethod>): Map<String, List<RequestMethod>> {
        val map = mutableMapOf<String, List<RequestMethod>>()
        this.forEach { path -> map[path] = method }
        return map
    }
}

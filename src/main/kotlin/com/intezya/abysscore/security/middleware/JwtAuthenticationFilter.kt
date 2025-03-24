package com.intezya.abysscore.security.middleware

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.security.public.PUBLIC_PATHS
import com.intezya.abysscore.security.service.JwtAuthenticationService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private const val AUTHORIZATION_HEADER = "Authorization"
private const val CONTENT_TYPE_JSON = "application/json"

@Component
class JwtAuthenticationFilter(
    private val jwtAuthenticationService: JwtAuthenticationService,
) : OncePerRequestFilter() {
    private val antPathMatcher = AntPathMatcher()
    private val log = LogFactory.getLog(this.javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response)
            return
        }

        log.debug("Processing authentication for request: ${request.requestURI}")

        val authHeader = request.getHeader(AUTHORIZATION_HEADER)
        val (jwtValid, jwtOrError) = jwtAuthenticationService.extractJwtFromHeader(authHeader)

        if (!jwtValid) {
            val errorCode = when (jwtOrError) {
                "Authentication required" -> "AUTH_REQUIRED"
                "Empty token" -> "AUTH_EMPTY_TOKEN"
                "Invalid token format" -> "AUTH_TOKEN_TOO_LONG"
                else -> "AUTH_ERROR"
            }
            val statusCode = if (jwtOrError == "Authentication required")
                HttpServletResponse.SC_UNAUTHORIZED else HttpServletResponse.SC_BAD_REQUEST

            sendErrorResponse(
                request,
                response,
                statusCode,
                jwtOrError,
                errorCode,
            )
            return
        }

        try {
            val (authenticated, userDetails) = jwtAuthenticationService.authenticateWithToken(jwtOrError)

            if (!authenticated || userDetails == null) {
                sendErrorResponse(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Access denied",
                    "AUTH_FAILED",
                )
                return
            }

            if (SecurityContextHolder.getContext().authentication == null) {
                val authToken = jwtAuthenticationService.createAuthenticationToken(userDetails, request)
                SecurityContextHolder.getContext().authentication = authToken
            }

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            log.error("Unexpected error during authentication", e)
            e.printStackTrace()
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Authentication error",
                "AUTH_INTERNAL_ERROR",
            )
        }
    }

    private fun sendErrorResponse(
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
        status: Int,
        message: String,
        errorCode: String? = null,
    ) {
        val errorResponse = mapOf(
            "status" to status,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "path" to httpRequest.requestURI,
        ).let {
            if (errorCode != null) it.plus("error_code" to errorCode) else it
        }

        val jsonResponse = ObjectMapper().writeValueAsString(errorResponse)

        response.status = status
        response.contentType = CONTENT_TYPE_JSON
        response.writer.write(jsonResponse)
        response.writer.flush()
    }

    private fun shouldSkipFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        return PUBLIC_PATHS.any { antPathMatcher.match(it, path) } ||
            antPathMatcher.match("/swagger-ui/**", path) ||
            request.method == HttpMethod.OPTIONS.name()
    }
}

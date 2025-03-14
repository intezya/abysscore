package com.intezya.abysscore.security.middleware

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.security.public.PUBLIC_PATHS
import com.intezya.abysscore.security.service.CustomUserDetailsService
import com.intezya.abysscore.security.utils.JwtUtils
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private const val BEARER_PREFIX = "Bearer "
private const val AUTHORIZATION_HEADER = "Authorization"
private const val CONTENT_TYPE_JSON = "application/json"

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtUtils,
    private val userDetailsService: CustomUserDetailsService,
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

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Authentication header found or invalid format")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required",
                "AUTH_REQUIRED",
            )
            return
        }

        val jwt = authHeader.substring(BEARER_PREFIX.length)

        if (jwt.isBlank()) {
            log.warn("Empty JWT token provided")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Empty token",
                "AUTH_EMPTY_TOKEN",
            )
            return
        }

        if (jwt.length > 10000) {
            log.warn("JWT token exceeds maximum allowed length")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid token format",
                "AUTH_TOKEN_TOO_LONG",
            )
            return
        }

        try {
            if (!authenticateWithToken(jwt, request, response)) {
                return
            }

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            log.error("Unexpected error during authentication", e)
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Authentication error",
                "AUTH_INTERNAL_ERROR",
            )
        }
    }

    private fun authenticateWithToken(
        jwt: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        try {
            val username = jwtService.extractUsername(jwt)

            if (SecurityContextHolder.getContext().authentication != null) {
                return true
            }

            val userDetails = try {
                userDetailsService.loadUserByUsername(username)
            } catch (e: UsernameNotFoundException) {
                log.warn("User not found: $username")
                sendErrorResponse(
                    request,
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied",
                    "AUTH_USER_NOT_FOUND",
                )
                return false
            }

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("Invalid JWT token for user: $username")
                sendErrorResponse(
                    request,
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied",
                    "AUTH_INVALID_TOKEN",
                )
                return false
            }

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities,
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken

            log.debug("User authenticated successfully: $username")
            return true
        } catch (e: ExpiredJwtException) {
            log.warn("JWT token expired")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Token expired",
                "AUTH_TOKEN_EXPIRED",
            )
            return false
        } catch (e: MalformedJwtException) {
            log.warn("Invalid JWT token format")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid token format",
                "AUTH_MALFORMED_TOKEN",
            )
            return false
        } catch (e: SignatureException) {
            log.warn("JWT signature validation failed")
            sendErrorResponse(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid token signature",
                "AUTH_INVALID_SIGNATURE",
            )
            return false
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

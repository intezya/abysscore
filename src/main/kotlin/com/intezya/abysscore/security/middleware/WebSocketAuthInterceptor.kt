package com.intezya.abysscore.security.middleware

import com.intezya.abysscore.security.service.CustomAuthenticationProvider
import com.intezya.abysscore.security.service.JwtAuthenticationService
import org.apache.commons.logging.LogFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

const val USER_AUTH_TOKEN = "user_auth_token"

class WebSocketAuthInterceptor(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val authenticationProvider: CustomAuthenticationProvider,
) : HandshakeInterceptor {
    private val logger = LogFactory.getLog(this.javaClass)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val authHeader = request.headers["Authorization"]?.firstOrNull()

        if (authHeader == null) {
            logger.warn("WebSocket: Missing Authorization header")
            return false
        }

        val (jwtValid, jwtOrError) = jwtAuthenticationService.extractJwtFromHeader(authHeader)

        if (!jwtValid) {
            logger.warn("WebSocket: $jwtOrError")
            return false
        }

        try {
            val (authenticated, userDetails) = jwtAuthenticationService.authenticateWithToken(jwtOrError)

            if (!authenticated || userDetails == null) {
                logger.warn("WebSocket: Authentication failed")
                return false
            }

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities,
            )

            SecurityContextHolder.getContext().authentication = authToken
            logger.info("WebSocket: User authenticated successfully: ${userDetails.username}")
            return true
        } catch (e: Exception) {
            logger.error("WebSocket: Unexpected error during authentication", e)
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }
}

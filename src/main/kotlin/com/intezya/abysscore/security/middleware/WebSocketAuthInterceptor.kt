package com.intezya.abysscore.security.middleware

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.security.service.JwtAuthenticationService
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.LogFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

const val USER_AUTHORIZATION = "user_authorization"

@Component
class WebSocketAuthInterceptor(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val objectMapper: ObjectMapper,
) : HandshakeInterceptor {
    private val logger = LogFactory.getLog(this.javaClass)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val authHeader = request.headers["Authorization"]?.firstOrNull()

        return if (authHeader == null) {
            sendRejectionMessage(response, "Missing Authorization header")
            false
        } else {
            handleTokenAuthentication(authHeader, response, attributes)
        }
    }

    private fun handleTokenAuthentication(
        authHeader: String,
        response: ServerHttpResponse,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val (jwtValid, jwtOrError) = jwtAuthenticationService.extractJwtFromHeader(authHeader)

        logger.debug("jwt valid: $jwtValid, jwt: $jwtOrError")

        if (!jwtValid) {
            sendRejectionMessage(response, jwtOrError)
            return false
        }

        return try {
            val (authenticated, userDetails) = jwtAuthenticationService.authenticateWithToken(jwtOrError)

            logger.debug("authenticated: $authenticated, user: $userDetails")

            if (!authenticated || userDetails == null) {
                sendRejectionMessage(response, "Authentication failed")
                false
            } else {
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities,
                )

                attributes[USER_AUTHORIZATION] = authToken
                logger.info("WebSocket: User authenticated successfully: ${userDetails.username}")
                true
            }
        } catch (e: Exception) {
            sendRejectionMessage(response, "Unexpected authentication error")
            logger.error("WebSocket: Unexpected error during authentication", e)
            false
        }
    }

    private fun sendRejectionMessage(response: ServerHttpResponse, reason: String) {
        try {
            val rejectionMessage = mapOf(
                "type" to "CONNECTION_REJECTED",
                "reason" to reason,
            )

            if (response is ServletServerHttpResponse) {
                response.servletResponse.status = HttpServletResponse.SC_UNAUTHORIZED
            }

            response.body.use { outputStream ->
                outputStream.write(objectMapper.writeValueAsBytes(rejectionMessage))
                outputStream.flush()
            }

            logger.warn("WebSocket connection rejected: $reason")
        } catch (e: Exception) {
            logger.error("Failed to send WebSocket rejection message", e)
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
        exception?.let {
            logger.error("WebSocket handshake error", it)
        }
    }
}

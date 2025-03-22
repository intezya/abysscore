package com.intezya.abysscore.config

import com.intezya.abysscore.controller.ClientWebsocketHandler
import com.intezya.abysscore.security.utils.JwtUtils
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

// TODO
@Configuration
@EnableWebSocket
class WebSocketConfig(private val jwtUtils: JwtUtils, private val clientWebsocketHandler: ClientWebsocketHandler) :
    WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(clientWebsocketHandler, "/websocket/hubs/client")
            .setAllowedOrigins("*")
            .addInterceptors(
                object : HandshakeInterceptor {
                    override fun beforeHandshake(
                        request: ServerHttpRequest,
                        response: ServerHttpResponse,
                        wsHandler: WebSocketHandler,
                        attributes: MutableMap<String, Any>,
                    ): Boolean {
                        val token =
                            request.headers.getFirst("Authorization")?.removePrefix("Bearer ")
                                ?: return false

                        // Validation skipped cause it autovalidates in WebSecurity (as middleware)
                        val userInfo = jwtUtils.isTokenValid(token)
                        attributes["user_info"] = userInfo
                        return true
                    }

                    override fun afterHandshake(
                        request: ServerHttpRequest,
                        response: ServerHttpResponse,
                        wsHandler: WebSocketHandler,
                        exception: Exception?,
                    ) {
                        // No-op
                    }
                },
            )
    }
}

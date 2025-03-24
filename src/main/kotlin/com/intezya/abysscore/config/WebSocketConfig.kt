package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.handlers.ChatWebSocketHandler
import com.intezya.abysscore.security.middleware.WebSocketAuthInterceptor
import com.intezya.abysscore.security.service.JwtAuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val objectMapper: ObjectMapper,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler(), "/match-chat")
            .setAllowedOriginPatterns("*")
            .addInterceptors(WebSocketAuthInterceptor(jwtAuthenticationService, objectMapper))
    }

    @Bean
    fun chatWebSocketHandler(): WebSocketHandler {
        return ChatWebSocketHandler()
    }
}

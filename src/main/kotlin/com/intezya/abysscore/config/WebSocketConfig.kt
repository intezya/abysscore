package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.security.middleware.WebSocketAuthInterceptor
import com.intezya.abysscore.security.service.JwtAuthenticationService
import com.intezya.abysscore.service.MainWebsocketConnectionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val objectMapper: ObjectMapper,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler(), "/hubs/main")
            .setAllowedOriginPatterns("*")
            .addInterceptors(WebSocketAuthInterceptor(jwtAuthenticationService, objectMapper))
    }

    @Bean
    fun chatWebSocketHandler(): WebSocketHandler = MainWebsocketConnectionService()

    @Bean
    fun servletServerContainerFactoryBean(): ServletServerContainerFactoryBean {
        val container = ServletServerContainerFactoryBean()
        container.setMaxSessionIdleTimeout(60000)
        container.setMaxTextMessageBufferSize(8192)
        container.setMaxBinaryMessageBufferSize(8192)
        return container
    }
}

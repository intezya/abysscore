package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.security.middleware.WebSocketAuthInterceptor
import com.intezya.abysscore.security.service.JwtAuthenticationService
import com.intezya.abysscore.service.DraftWebSocketConnectionService
import com.intezya.abysscore.service.MainWebSocketConnectionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val objectMapper: ObjectMapper,
    private val mainWebSocketConnectionService: MainWebSocketConnectionService,
    private val draftWebSocketConnectionService: DraftWebSocketConnectionService,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(mainWebSocketConnectionService, "/hubs/main")
            .setAllowedOriginPatterns("*")
            .addInterceptors(webSocketAuthInterceptor())
        registry.addHandler(draftWebSocketConnectionService, "/hubs/draft")
            .setAllowedOriginPatterns("*")
            .addInterceptors(webSocketAuthInterceptor())
    }

    @Bean
    fun servletServerContainerFactoryBean(): ServletServerContainerFactoryBean {
        val container = ServletServerContainerFactoryBean()
        container.setMaxSessionIdleTimeout(60000)
        container.setMaxTextMessageBufferSize(8192)
        container.setMaxBinaryMessageBufferSize(8192)
        return container
    }

    @Bean
    fun webSocketAuthInterceptor() = WebSocketAuthInterceptor(
        jwtAuthenticationService = jwtAuthenticationService,
        objectMapper = objectMapper,
    )
}

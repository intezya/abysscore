package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.service.DraftWebSocketConnectionService
import com.intezya.abysscore.service.MainWebSocketConnectionService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebsocketMessageBrokerConfig(
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun mainWebSocketMessageService() = MainWebSocketConnectionService(
        eventPublisher = eventPublisher,
        objectMapper = objectMapper,
    )

    @Bean
    fun draftWebSocketMessageService() = DraftWebSocketConnectionService(
        eventPublisher = eventPublisher,
        objectMapper = objectMapper,
    )
}

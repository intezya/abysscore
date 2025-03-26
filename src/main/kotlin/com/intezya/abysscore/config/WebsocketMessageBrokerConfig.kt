package com.intezya.abysscore.config

import com.intezya.abysscore.service.MainWebsocketConnectionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebsocketMessageBrokerConfig(
    private val mainWebsocketConnectionService: MainWebsocketConnectionService,
) {
    @Bean
    fun mainWebsocketMessageService() = mainWebsocketConnectionService
}

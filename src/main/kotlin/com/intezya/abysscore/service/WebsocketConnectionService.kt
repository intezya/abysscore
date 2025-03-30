package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationEventPublisher

class MainWebSocketConnectionService(eventPublisher: ApplicationEventPublisher, objectMapper: ObjectMapper) :
    BaseWebSocketConnectionService(
        eventPublisher,
        objectMapper,
    )

class DraftWebSocketConnectionService(eventPublisher: ApplicationEventPublisher, objectMapper: ObjectMapper) :
    BaseWebSocketConnectionService(
        eventPublisher,
        objectMapper,
    )

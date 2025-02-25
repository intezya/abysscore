package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {
    fun <T> sendActionEvent(
        event: T,
        eventKey: String,
        topic: String,
    ) {
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send(topic, eventKey, message)
    }
}

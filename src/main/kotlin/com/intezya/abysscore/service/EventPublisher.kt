package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    fun sendActionEvent(
        event: Any,
        eventKey: String,
        topic: String,
    ) {
        coroutineScope.launch {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(topic, eventKey, message)
        }
    }
}

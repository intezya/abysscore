package com.intezya.abysscore.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val ACTION_TOPIC = "user-action-events"

        data class UserActionEvent(
            @field:JsonProperty("username")
            val username: String,
            @field:JsonProperty("ip")
            val ip: String,
            @field:JsonProperty("event_type")
            val eventType: UserActionEventType,
            @field:JsonProperty("is_success")
            val isSuccess: Boolean,
            @field:JsonProperty("hwid")
            val hwid: String,
            @field:JsonProperty("timestamp")
            val timestamp: Instant = Instant.now(),
        )

        enum class UserActionEventType {
            REGISTRATION,
            LOGIN,
            CLIENT_WEBSOCKET_CONNECT,
            CLIENT_WEBSOCKET_DISCONNECT,
        }
    }

    fun sendRegistrationEvent(
        username: String,
        ip: String,
        hwid: String,
        isSuccess: Boolean
    ) {
        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = UserActionEventType.REGISTRATION,
            hwid = hwid,
            isSuccess = isSuccess
        )
        sendActionEvent(event)
    }

    fun sendLoginEvent(
        username: String,
        ip: String,
        hwid: String,
        isSuccess: Boolean
    ) {
        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = UserActionEventType.LOGIN,
            hwid = hwid,
            isSuccess = isSuccess,
        )
        sendActionEvent(event)
    }

    fun sendClientWebsocketEvent(
        username: String,
        ip: String,
        hwid: String,
        isSuccess: Boolean,
        connect: Boolean,
    ) {
        if (connect) {
            val event = UserActionEvent(
                username = username,
                ip = ip,
                eventType = UserActionEventType.CLIENT_WEBSOCKET_CONNECT,
                hwid = hwid,
                isSuccess = isSuccess,
            )
            sendActionEvent(event)
            return
        }

        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = UserActionEventType.CLIENT_WEBSOCKET_DISCONNECT,
            hwid = hwid,
            isSuccess = isSuccess,
        )
        sendActionEvent(event)
    }

    private fun sendActionEvent(event: UserActionEvent) {
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send(ACTION_TOPIC, event.username, message)
    }
}

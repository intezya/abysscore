package com.intezya.abysscore.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.message.websocket.WebsocketDisconnectMessage
import com.intezya.abysscore.security.middleware.USER_AUTHORIZATION
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

typealias UserID = Long

class ChatWebSocketHandler : TextWebSocketHandler() {
    private val objectMapper = ObjectMapper()

    companion object {
        private val logger = LoggerFactory.getLogger(ChatWebSocketHandler::class.java)
        private const val DISCONNECT_REASON = "Connected from another client"
    }

    private val sessions = ConcurrentHashMap<UserID, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = extractCurrentUser(session.attributes)

        synchronized(sessions) {
            val existingSession = sessions[user.id]
            existingSession?.let {
                sendMessage(it, WebsocketDisconnectMessage(reason = DISCONNECT_REASON))
                it.close()
            }

            sessions[user.id] = session
        }

        logger.debug("Connection established for user: {}", user.id)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val user = extractCurrentUser(session.attributes)
        val payload = message.payload

        logger.debug("Message received from user {}: {}", user.id, payload)

        try {
            val responseMessage = TextMessage("Echo: $payload")
            session.sendMessage(responseMessage)
        } catch (e: Exception) {
            logger.error("Error processing message from user {}: {}", user.id, e.message, e)
            session.sendMessage(TextMessage("Error processing your message"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = extractCurrentUser(session.attributes)

        synchronized(sessions) {
            sessions.remove(user.id)
        }

        logger.debug("Connection closed for user: {} with status {}", user.id, status.code)
    }

    private fun sendMessage(session: WebSocketSession, messageContent: Any) {
        runCatching {
            if (session.isOpen) {
                val message = objectMapper.writeValueAsString(messageContent)
                session.sendMessage(TextMessage(message))
            }
        }.onFailure { e ->
            logger.error("Failed to send message: {}", e.message, e)
        }
    }

    private fun extractCurrentUser(attributes: Map<String, Any>): User {
        return runCatching {
            val authToken = attributes[USER_AUTHORIZATION] as UsernamePasswordAuthenticationToken
            authToken.principal as User
        }.getOrElse {
            logger.error("No authenticated user found")
            throw IllegalStateException("No authenticated user found")
        }
    }
}

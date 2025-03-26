package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.message.websocket.WebsocketDisconnectMessageBase
import com.intezya.abysscore.security.middleware.USER_AUTHORIZATION
import com.intezya.abysscore.service.interfaces.WebsocketMessageBroker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

private const val DISCONNECT_REASON = "Connected from another client"

class WebsocketConnectionService :
    TextWebSocketHandler(),
    WebsocketMessageBroker<Long> {
    private val objectMapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(WebsocketConnectionService::class.java)
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = extractCurrentUser(session.attributes)

        sessions.compute(user.id) { _, existingSession ->
            existingSession?.let {
                runCatching {
                    sendMessage(it, WebsocketDisconnectMessageBase(reason = DISCONNECT_REASON))
                    it.close()
                }.onFailure { e ->
                    logger.warn("Error closing existing session: ${e.message}")
                }
            }
            session
        }

        logger.debug("Connection established for user: ${user.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = extractCurrentUser(session.attributes)

        sessions.remove(user.id)
        logger.debug("Connection closed for user: ${user.id} with status ${status.code}")
    }

    override fun sendToUser(userId: Long, messageContent: Any) {
        sessions[userId]?.let { session ->
            sendMessage(session, messageContent)
        }
    }

    override fun getOnline(): Int {
        return sessions.size
    }

    override fun broadcast(messageContent: Any) = broadcast(messageContent, except = emptyList())

    override fun broadcast(messageContent: Any, except: List<Long>) {
        runBlocking {
            sessions.map { (key, session) ->
                async(Dispatchers.IO) {
                    if (!session.isOpen) {
                        sessions.remove(key)
                    } else if (!except.contains(key)) {
                        runCatching {
                            sendMessage(session, messageContent)
                        }.onFailure { e ->
                            logger.error("Failed to send broadcast message to user $key: ${e.message}")
                        }
                    }
                }
            }.forEach { it.await() }
        }
    }

    private fun sendMessage(session: WebSocketSession, messageContent: Any) {
        runCatching {
            if (session.isOpen) {
                val message = objectMapper.writeValueAsString(messageContent)
                session.sendMessage(TextMessage(message))
            }
        }.onFailure { e ->
            logger.error("Failed to send message: ${e.message}")
        }
    }

    private fun extractCurrentUser(attributes: Map<String, Any>): User = runCatching {
        val authToken = attributes[USER_AUTHORIZATION] as UsernamePasswordAuthenticationToken
        authToken.principal as User
    }.getOrElse {
        logger.error("No authenticated user found")
        throw IllegalStateException("No authenticated user found")
    }
}

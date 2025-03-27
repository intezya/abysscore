package com.intezya.abysscore.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.event.useraction.UserConnectedEvent
import com.intezya.abysscore.event.useraction.UserDisconnectedEvent
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.message.websocket.WebsocketDisconnectMessageBase
import com.intezya.abysscore.security.middleware.USER_AUTHORIZATION
import com.intezya.abysscore.service.interfaces.WebsocketMessageBroker
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private const val DISCONNECT_REASON = "Connected from another client"
private const val DISCONNECT_REASON_NEW_SESSION = "New connection established"

@Service
class MainWebsocketConnectionService(
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) :
    TextWebSocketHandler(),
    WebsocketMessageBroker<Long> {

    private val logger = LoggerFactory.getLogger(MainWebsocketConnectionService::class.java)
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = extractCurrentUser(session) ?: run {
            logger.warn("Connection established but no authenticated user found. Closing session ${session.id}")
            closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION.withReason("User not authenticated"))
            return
        }
        val userId = user.id

        logger.debug("Attempting to establish connection for user: $userId, session: ${session.id}")

        val oldSession = sessions.put(userId, session)

        if (oldSession != null && oldSession.id != session.id) {
            logger.warn("User $userId already had an active session (${oldSession.id}). Closing the old session.")
            sendDisconnectNotification(userId, oldSession, DISCONNECT_REASON_NEW_SESSION)
            closeSessionQuietly(oldSession, CloseStatus.POLICY_VIOLATION.withReason(DISCONNECT_REASON_NEW_SESSION))
        }

        eventPublisher.publishEvent(UserConnectedEvent(this, user))
        logger.info("Connection established for user: $userId, session: ${session.id}. Total online: ${sessions.size}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = extractCurrentUser(session) ?: run {
            val entry = sessions.entries.find { it.value.id == session.id }
            if (entry != null) {
                val userId = entry.key
                val removed = sessions.remove(userId, session)
                if (removed) {
                    logger.warn("Connection closed for potentially unauthenticated session ${session.id} associated with user $userId. Status: ${status.code}. Total online: ${sessions.size}")
                } else {
                    logger.debug("Connection closed for stale/replaced session ${session.id} associated with user $userId. Status: ${status.code}. No removal needed.")
                }
            } else {
                logger.debug("Connection closed for session ${session.id} with no associated user found in active sessions. Status: ${status.code}")
            }
            return
        }

        val userId = user.id
        val removed = sessions.remove(userId, session)

        if (removed) {
            eventPublisher.publishEvent(UserDisconnectedEvent(this, user))
            logger.info("Connection closed for user: $userId, session: ${session.id}. Status: ${status.code}. Total online: ${sessions.size}")
        } else {
            logger.debug("Connection closed for stale/replaced session ${session.id} for user $userId. Status: ${status.code}. No event published as it wasn't the active session.")
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}: ${exception.message}", exception)
        val user = extractCurrentUser(session)
        if (user != null) {
            val userId = user.id
            val removed = sessions.remove(userId, session)
            if (removed) {
                eventPublisher.publishEvent(UserDisconnectedEvent(this, user))
                logger.info("Session removed for user $userId due to transport error. Total online: ${sessions.size}")
            }
        } else {
            logger.warn("Transport error for session ${session.id} with no associated user.")
        }
        closeSessionQuietly(session, CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    override fun sendToUser(userId: Long, messageContent: Any) {
        val session = sessions[userId]
        if (session == null) {
            logger.debug("Cannot send message to user $userId: No active session found.")
            return
        }

        val textMessage = createTextMessage(messageContent) ?: return
        sendMessageInternal(userId, session, textMessage)
    }

    override fun getOnline(): Int {
        return sessions.size
    }

    override fun broadcast(messageContent: Any) {
        broadcast(messageContent, emptyList())
    }

    override fun broadcast(messageContent: Any, except: List<Long>) {
        val textMessage = createTextMessage(messageContent) ?: return

        val recipientCount = sessions.size - except.size
        if (recipientCount <= 0) {
            logger.debug("Broadcast skipped: No recipients after exceptions.")
            return
        }

        logger.debug("Broadcasting message to $recipientCount user(s) (excluding ${except.size}).")
        val exceptions = except.toSet()

        sessions.forEach { (userId, session) ->
            if (userId !in exceptions) {
                sendMessageInternal(userId, session, textMessage)
            }
        }
    }

    private fun createTextMessage(messageContent: Any): TextMessage? {
        return try {
            TextMessage(objectMapper.writeValueAsString(messageContent))
        } catch (e: JsonProcessingException) {
            logger.error("Failed to serialize message content to JSON: ${e.message}", e)
            null
        }
    }

    private fun sendMessageInternal(userId: Long, session: WebSocketSession, message: TextMessage) {
        if (!session.isOpen) {
            logger.warn("Attempted to send message to closed session ${session.id} for user $userId. Removing session.")
            sessions.remove(userId, session) // Use conditional remove
            return
        }

        try {
            synchronized(session) {
                if (session.isOpen) {
                    session.sendMessage(message)
                    logger.trace("Message sent to user $userId, session ${session.id}")
                } else {
                    logger.warn("Session ${session.id} for user $userId closed before message could be sent (inside synchronized block).")
                    sessions.remove(userId, session)
                }
            }
        } catch (e: IOException) {
            logger.error("Failed to send message to user $userId, session ${session.id}: ${e.message}", e)
            closeSessionQuietly(session, CloseStatus.GOING_AWAY.withReason("Failed to send message"))
            sessions.remove(userId, session)
            extractCurrentUser(session)?.let {
                eventPublisher.publishEvent(UserDisconnectedEvent(this, it))
                logger.info("Session removed for user $userId due to send error. Total online: ${sessions.size}")
            }
        } catch (e: Exception) {
            logger.error("Unexpected error sending message to user $userId, session ${session.id}: ${e.message}", e)
            closeSessionQuietly(session, CloseStatus.SERVER_ERROR.withReason("Unexpected send error"))
            sessions.remove(userId, session)
            extractCurrentUser(session)?.let { // Publish disconnect if we know the user
                eventPublisher.publishEvent(UserDisconnectedEvent(this, it))
                logger.info("Session removed for user $userId due to unexpected send error. Total online: ${sessions.size}")
            }
        }
    }

    private fun sendDisconnectNotification(userId: Long, session: WebSocketSession, reason: String) {
        val disconnectMessage = WebsocketDisconnectMessageBase(reason = reason)
        val textMessage = createTextMessage(disconnectMessage)
        if (textMessage != null) {
            if (session.isOpen) {
                try {
                    session.sendMessage(textMessage)
                    logger.debug("Sent disconnect notification to old session ${session.id} for user $userId")
                } catch (e: IOException) {
                    logger.warn("Failed to send disconnect notification to old session ${session.id}: ${e.message}")
                }
            }
        }
    }

    private fun closeSessionQuietly(session: WebSocketSession?, status: CloseStatus) {
        session ?: return
        if (session.isOpen) {
            try {
                session.close(status)
            } catch (e: IOException) {
                logger.warn("Error closing session ${session.id}: ${e.message}")
            } catch (e: Exception) {
                logger.warn("Unexpected error closing session ${session.id}: ${e.message}")
            }
        }
    }

    private fun extractCurrentUser(session: WebSocketSession): User? = extractCurrentUser(session.attributes)

    private fun extractCurrentUser(attributes: Map<String, Any>): User? = runCatching {
        when (val authentication = attributes[USER_AUTHORIZATION]) {
            is UsernamePasswordAuthenticationToken -> authentication.principal as? User
            else -> {
                logger.warn("Cannot extract user: Authentication object not found or not of expected type in session attributes. Found: ${authentication?.javaClass?.name}")
                null
            }
        }
    }.onFailure { e ->
        logger.error("Error extracting user from session attributes: ${e.message}", e)
    }.getOrNull()
}

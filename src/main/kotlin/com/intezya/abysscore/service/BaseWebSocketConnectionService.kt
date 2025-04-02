package com.intezya.abysscore.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.event.useraction.UserConnectedEvent
import com.intezya.abysscore.event.useraction.UserDisconnectedEvent
import com.intezya.abysscore.model.entity.user.User
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

@Service
class BaseWebSocketConnectionService(
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler(),
    WebsocketMessageBroker<Long> {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = extractCurrentUser(session) ?: run {
            closeUnauthenticatedSession(session)
            return
        }

        handleNewConnection(user, session)
    }

    private fun handleNewConnection(user: User, session: WebSocketSession) {
        val userId = user.id

        sessions[userId]?.let { oldSession ->
            logger.warn("User $userId already had an active session (${oldSession.id}). Closing the old session.")
            sendDisconnectNotification(userId, oldSession, "New connection established")
            closeSessionQuietly(oldSession, CloseStatus.POLICY_VIOLATION.withReason("New connection"))
        }

        sessions[userId] = session
        eventPublisher.publishEvent(UserConnectedEvent(this, user))
        logger.info("Connection established for user: $userId, session: ${session.id}. Total online: ${sessions.size}")
    }

    private fun closeUnauthenticatedSession(session: WebSocketSession) {
        logger.warn("Connection established but no authenticated user found. Closing session ${session.id}")
        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION.withReason("User not authenticated"))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = extractCurrentUser(session)

        if (user == null) {
            findAndRemoveUnknownSession(session, status)
            return
        }

        val userId = user.id
        if (sessions.remove(userId, session)) {
            eventPublisher.publishEvent(UserDisconnectedEvent(this, user))
            logger.info(
                "Connection closed for user: $userId, session: ${session.id}. Status: ${status.code}. Total online: ${sessions.size}",
            )
        }
    }

    private fun findAndRemoveUnknownSession(session: WebSocketSession, status: CloseStatus) {
        sessions.entries.find { it.value.id == session.id }?.let { entry ->
            val userId = entry.key
            if (sessions.remove(userId, entry.value)) {
                logger.warn(
                    "Connection closed for session ${session.id} associated with user $userId. Status: ${status.code}. Total online: ${sessions.size}",
                )
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val user = extractCurrentUser(session)
        user?.let { handleUserTransportError(it, session, exception) }
            ?: logger.warn("Transport error for session ${session.id} with no associated user.")

        closeSessionQuietly(session, CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    private fun handleUserTransportError(user: User, session: WebSocketSession, exception: Throwable) {
        val userId = user.id
        logger.error("Transport error for session ${session.id} for user $userId: ${exception.message}", exception)

        sessions.remove(userId, session)
        eventPublisher.publishEvent(UserDisconnectedEvent(this, user))
        logger.info("Session removed for user $userId due to transport error. Total online: ${sessions.size}")
    }

    override fun sendToUser(userId: Long, messageContent: Any) {
        sessions[userId]?.let { session ->
            createTextMessage(messageContent)?.let { textMessage ->
                sendMessageInternal(userId, session, textMessage)
            }
        } ?: logger.debug("Cannot send message to user $userId: No active session found.")
    }

    override fun getOnline(): Int = sessions.size

    override fun broadcast(messageContent: Any) = broadcast(messageContent, emptyList())

    override fun broadcast(messageContent: Any, except: List<Long>) {
        val textMessage = createTextMessage(messageContent) ?: return
        val exceptions = except.toSet()

        val recipientCount = sessions.size - exceptions.size
        if (recipientCount <= 0) {
            logger.debug("Broadcast skipped: No recipients after exceptions.")
            return
        }

        logger.debug("Broadcasting message to $recipientCount user(s) (excluding ${exceptions.size}).")

        sessions.forEach { (userId, session) ->
            if (userId !in exceptions) {
                sendMessageInternal(userId, session, textMessage)
            }
        }
    }

    private fun createTextMessage(messageContent: Any): TextMessage? = try {
        TextMessage(objectMapper.writeValueAsString(messageContent))
    } catch (e: JsonProcessingException) {
        logger.error("Failed to serialize message content to JSON: ${e.message}", e)
        null
    }

    private fun sendMessageInternal(userId: Long, session: WebSocketSession, message: TextMessage) {
        if (!session.isOpen) {
            logger.warn("Attempted to send message to closed session ${session.id} for user $userId. Removing session.")
            sessions.remove(userId, session)
            return
        }

        try {
            synchronized(session) {
                if (session.isOpen) {
                    session.sendMessage(message)
                    logger.trace("Message sent to user $userId, session ${session.id}")
                } else {
                    logger.warn("Session ${session.id} for user $userId closed before message could be sent.")
                    sessions.remove(userId, session)
                }
            }
        } catch (e: Exception) {
            handleMessageSendError(userId, session, e)
        }
    }

    private fun handleMessageSendError(userId: Long, session: WebSocketSession, exception: Exception) {
        logger.error("Error sending message to user $userId, session ${session.id}: ${exception.message}", exception)

        closeSessionQuietly(session, CloseStatus.GOING_AWAY.withReason("Failed to send message"))
        sessions.remove(userId, session)

        extractCurrentUser(session)?.let { user ->
            eventPublisher.publishEvent(UserDisconnectedEvent(this, user))
            logger.info("Session removed for user $userId due to send error. Total online: ${sessions.size}")
        }
    }

    private fun sendDisconnectNotification(userId: Long, session: WebSocketSession, reason: String) {
        val disconnectMessage = WebsocketDisconnectMessageBase(reason = reason)
        createTextMessage(disconnectMessage)?.let { textMessage ->
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
            } catch (e: Exception) {
                logger.warn("Error closing session ${session.id}: ${e.message}")
            }
        }
    }

    private fun extractCurrentUser(session: WebSocketSession): User? = extractCurrentUser(session.attributes)

    private fun extractCurrentUser(attributes: Map<String, Any>): User? = runCatching {
        (attributes[USER_AUTHORIZATION] as? UsernamePasswordAuthenticationToken)?.principal as? User
    }.onFailure { e ->
        logger.error("Error extracting user from session attributes: ${e.message}", e)
    }.getOrNull()
}

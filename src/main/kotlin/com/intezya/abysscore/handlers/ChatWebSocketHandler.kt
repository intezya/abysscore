package com.intezya.abysscore.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.message.websocket.WebsocketDisconnectMessage
import org.apache.commons.logging.LogFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

typealias UserID = Long

class ChatWebSocketHandler : TextWebSocketHandler() {
    private val logger = LogFactory.getLog(this.javaClass)
    private val sessions = ConcurrentHashMap<UserID, WebSocketSession>()
    private val objectMapper = ObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = SecurityContextHolder.getContext().authentication.principal as User

        val existingSession = sessions[user.id]
        if (existingSession != null) {
            sendMessage(existingSession, WebsocketDisconnectMessage(reason = "Connected from other client"))
            existingSession.close()
        }


        logger.debug("Connection established: ${user.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        logger.debug("Message received: $payload from ${session.id}")
        session.sendMessage(TextMessage("Echo: $payload"))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        sessions.remove(user.id)
        logger.debug("Connection closed: ${user.id} with status ${status.code}")
    }

    private fun sendMessage(session: WebSocketSession, messageContent: Any) {
        if (session.isOpen) {
            val message = objectMapper.writeValueAsString(messageContent)
            session.sendMessage(TextMessage(message))
        }
    }
}

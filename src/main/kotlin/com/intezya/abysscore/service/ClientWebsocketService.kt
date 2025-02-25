package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.dto.websocket.UserSessionDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage

@Service
class ClientWebsocketService(
    private val objectMapper: ObjectMapper,
    private val userEventService: UserEventService,
) {
    private val sessions = mutableMapOf<Int, UserSessionDTO>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val closingMessage = TextMessage(objectMapper.writeValueAsString(mapOf("type" to "closing")))

    fun addConnection(session: UserSessionDTO) {
        val existingSession = sessions[session.id]
        try {
            existingSession?.connection?.sendMessage(closingMessage)
            existingSession?.connection?.close()
        } catch (_: IllegalStateException) {
        }
        sessions[session.id] = session
        sendEvent(session, success = true, connect = true)
        println("Connections: ${sessions.size}")
    }

    fun removeConnection(clientId: Int) {
        val session = sessions.remove(clientId)

        if (session != null) {
            session.connection.close()
            sendEvent(session, success = true, connect = false)
        }
        println("Connections: ${sessions.size}")
    }

    fun sendEvent(session: UserSessionDTO, success: Boolean, connect: Boolean) {
        coroutineScope.run {
            userEventService.sendClientWebsocketEvent(
                username = session.username,
                ip = session.ip,
                hwid = session.hwid,
                isSuccess = success,
                connect = connect,
            )
        }
    }
}

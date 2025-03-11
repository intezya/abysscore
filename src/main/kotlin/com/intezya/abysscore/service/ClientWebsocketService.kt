package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.dto.websocket.UserSessionDTO
import com.intezya.abysscore.security.service.AuthDTO
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Service
class ClientWebsocketService(
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
) {
    private val sessions = mutableSetOf<UserSessionDTO>()
    private val closingMessage = TextMessage(objectMapper.writeValueAsString(mapOf("type" to "closing")))

    fun addConnection(session: WebSocketSession) {
        val existingSession = sessions.firstOrNull { it.id.toString() == session.id }
        try {
            existingSession?.connection?.sendMessage(closingMessage)
            existingSession?.connection?.close()
        } catch (_: IllegalStateException) {
        }
        sessions.add(getUserSession(session))
    }

    fun removeConnection(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val existingSession = sessions.firstOrNull { it.id.toString() == session.id }

        existingSession?.connection?.close()

        sessions.remove(existingSession)
    }

    private fun getUserSession(
        session: WebSocketSession,
    ): UserSessionDTO {
        val principal = session.principal ?: throw IllegalStateException("No authenticated principal found")

        val authDTO = when (principal) {
            is Authentication ->
                principal.principal as? AuthDTO
                    ?: throw IllegalStateException("Principal is not AuthDTO")

            else -> throw IllegalStateException("Unexpected principal type: ${principal.javaClass}")
        }

        val user = userService.findUserWithThrow(authDTO.id)

        return UserSessionDTO(
            id = user.id!!,
            username = user.username,
//            ip = jwtUtils.getClientIp(session),
            ip = "some",
            hwid = user.hwid!!,
            connection = session,
        )
    }
}

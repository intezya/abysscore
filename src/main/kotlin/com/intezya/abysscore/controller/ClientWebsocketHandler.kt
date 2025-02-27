package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.dto.websocket.UserSessionDTO
import com.intezya.abysscore.service.ClientWebsocketService
import com.intezya.abysscore.utils.auth.AuthUtils
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ClientWebsocketHandler(
    private val clientWebsocketService: ClientWebsocketService,
    private val authUtils: AuthUtils,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        clientWebsocketService.addConnection(getUserSession(session))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {}

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        clientWebsocketService.removeConnection(getUserInfo(session).id)
    }

    private fun getUserInfo(session: WebSocketSession): UserAuthInfoDTO {
        return session.attributes["user_info"] as UserAuthInfoDTO
    }

    private fun getUserSession(session: WebSocketSession): UserSessionDTO {
        val userInfo = getUserInfo(session)
        return UserSessionDTO(
            id = userInfo.id,
            username = userInfo.username,
            ip = authUtils.getClientIp(session),
            hwid = userInfo.hwid,
            connection = session,
        )
    }
}

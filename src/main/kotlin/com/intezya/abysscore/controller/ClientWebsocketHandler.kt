package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.websocket.UserSessionDTO
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import com.intezya.abysscore.security.jwt.JwtUtils
import com.intezya.abysscore.service.ClientWebsocketService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ClientWebsocketHandler(
    private val clientWebsocketService: ClientWebsocketService,
    private val jwtUtils: JwtUtils,
) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        clientWebsocketService.addConnection(getUserSession(session))
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        clientWebsocketService.removeConnection(getUserInfo(session).id)
    }

    private fun getUserInfo(session: WebSocketSession): UserAuthInfoDTO = session.attributes["user_info"] as UserAuthInfoDTO

    private fun getUserSession(session: WebSocketSession): UserSessionDTO {
        val userInfo = getUserInfo(session)
        return UserSessionDTO(
            id = userInfo.id,
            username = userInfo.username,
            ip = jwtUtils.getClientIp(session),
            hwid = userInfo.hwid,
            connection = session,
        )
    }
}

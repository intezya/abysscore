package com.intezya.abysscore.controller

import com.intezya.abysscore.service.ClientWebsocketService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ClientWebsocketHandler(private val clientWebsocketService: ClientWebsocketService) : TextWebSocketHandler() {
    override fun afterConnectionEstablished(session: WebSocketSession) {
        clientWebsocketService.addConnection(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        TODO("not implemented")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        clientWebsocketService.removeConnection(session, status)
    }
}

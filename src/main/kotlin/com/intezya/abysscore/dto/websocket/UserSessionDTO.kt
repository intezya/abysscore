package com.intezya.abysscore.dto.websocket

import org.springframework.web.socket.WebSocketSession

data class UserSessionDTO(
    val id: Long,
    val username: String,
    val ip: String,
    val hwid: String,
    val connection: WebSocketSession,
)

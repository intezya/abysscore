package com.intezya.abysscore.model.message.websocket

data class WebsocketDisconnectMessageBase(val reason: String) {
    val message: String = "User logged in"
}

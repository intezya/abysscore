package com.intezya.abysscore.model.message.websocket

class WebsocketDisconnectMessage(reason: String) : WebSocketMessage(
    message = Messages.USER_DISCONNECTED,
    reason = reason,
)

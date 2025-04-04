package com.intezya.abysscore.model.message.websocket.user.action

import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseUserActionMessage : BaseWebsocketMessage() {
    override val messageType = "match"
    override val messageSubtype = "matchmaking"
}

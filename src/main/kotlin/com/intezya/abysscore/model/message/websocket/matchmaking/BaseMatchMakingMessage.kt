package com.intezya.abysscore.model.message.websocket.matchmaking

import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseMatchMakingMessage : BaseWebsocketMessage() {
    override val messageType = "match"
    override val messageSubtype = "matchmaking"
}

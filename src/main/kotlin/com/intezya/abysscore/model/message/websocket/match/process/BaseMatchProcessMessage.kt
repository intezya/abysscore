package com.intezya.abysscore.model.message.websocket.match.process

import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseMatchProcessMessage : BaseWebsocketMessage() {
    override val messageType = "match"
    override val messageSubtype = "process"
}

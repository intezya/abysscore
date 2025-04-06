package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseDraftProcessMessage : BaseWebsocketMessage() {
    override val messageType: String = "draft"
    override val messageSubtype: String = "process"
}

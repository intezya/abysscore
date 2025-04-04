package com.intezya.abysscore.model.message.websocket.match.process

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseMatchProcessMessage : BaseWebsocketMessage() {
    @field:JsonProperty("message_type")
    override val messageType = "match"

    @field:JsonProperty("message_subtype")
    override val messageSubtype = "process"
}

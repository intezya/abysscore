package com.intezya.abysscore.model.message.websocket

import com.fasterxml.jackson.annotation.JsonProperty

abstract class BaseWebsocketMessage {
    @field:JsonProperty("message_type")
    protected open val messageType = "match"

    @field:JsonProperty("message_subtype")
    protected open val messageSubtype = "invite"
}

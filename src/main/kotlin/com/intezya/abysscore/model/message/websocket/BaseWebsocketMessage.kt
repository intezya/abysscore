package com.intezya.abysscore.model.message.websocket

import com.intezya.abysscore.utils.annotation.meta.InheritableMessageSubtype
import com.intezya.abysscore.utils.annotation.meta.InheritableMessageType

abstract class BaseWebsocketMessage {
    @field:InheritableMessageType
    protected open val messageType = "type"

    @field:InheritableMessageSubtype
    protected open val messageSubtype = "subtype"
}

package com.intezya.abysscore.model.message.websocket.user.account

import com.intezya.abysscore.model.message.websocket.BaseWebsocketMessage

abstract class BaseAccountMessage : BaseWebsocketMessage() {
    override val messageType: String = "account"
    override val messageSubtype: String = "ban"
}

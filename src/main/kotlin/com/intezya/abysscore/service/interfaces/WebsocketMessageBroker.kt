package com.intezya.abysscore.service.interfaces

interface WebsocketMessageBroker : WebsocketOnlineProvider {
    fun sendToUser(userId: Long, messageContent: Any)
    fun broadcast(messageContent: Any)
}

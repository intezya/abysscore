package com.intezya.abysscore.service.interfaces

interface WebsocketMessageBroker<K> : WebsocketOnlineProvider {
    fun sendToUser(userId: Long, messageContent: Any)
    fun broadcast(messageContent: Any)
    fun broadcast(messageContent: Any, except: List<K>)
}

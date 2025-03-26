package com.intezya.abysscore.service

import com.intezya.abysscore.model.message.websocket.UserLoggedInMessage
import com.intezya.abysscore.model.message.websocket.UserLoggedOutMessage
import com.intezya.abysscore.service.interfaces.WebsocketMessageBroker
import org.springframework.stereotype.Service

@Service
class WebsocketNotificationService(
    private val mainWebsocketMessageService: WebsocketMessageBroker<Long>,
//    private val matchWebsocketMessageService: WebsocketMessageBroker<Long>,
//    private val draftWebsocketMessageService: WebsocketMessageBroker<Long>,
) {

    fun userLoggedIn(userId: Long, username: String) {
        val message = UserLoggedInMessage(
            username = username,
            currentOnline = mainWebsocketMessageService.getOnline(),
        )
        mainWebsocketMessageService.broadcast(message, except = listOf(userId))
    }

    fun userLoggedOut(userId: Long, username: String) {
        val message = UserLoggedOutMessage(
            username = username,
            currentOnline = mainWebsocketMessageService.getOnline(),
        )
        mainWebsocketMessageService.broadcast(message, except = listOf(userId))
    }
}

package com.intezya.abysscore.service

import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteAcceptedEvent
import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteReceivedEvent
import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteRejectedEvent
import com.intezya.abysscore.model.message.websocket.useractions.UserLoggedInMessage
import com.intezya.abysscore.model.message.websocket.useractions.UserLoggedOutMessage
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

    fun inviteReceived(userId: Long, inviteId: Long, inviterUsername: String) {
        val message = MatchInviteReceivedEvent(
            inviteId = inviteId,
            inviterUsername = inviterUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun inviteAccepted(userId: Long, inviteId: Long, inviteeUsername: String) {
        val message = MatchInviteAcceptedEvent(
            inviteId = inviteId,
            inviteeUsername = inviteeUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun inviteRejected(userId: Long, inviteId: Long, inviteeUsername: String) {
        val message = MatchInviteRejectedEvent(
            inviteId = inviteId,
            inviteeUsername = inviteeUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }
}

package com.intezya.abysscore.listener

import com.intezya.abysscore.event.matchinvite.InviteAcceptedEvent
import com.intezya.abysscore.event.matchinvite.InviteReceivedEvent
import com.intezya.abysscore.event.matchinvite.InviteRejectedEvent
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchInviteEventListener(private val websocketNotificationService: WebsocketNotificationService) {
    @EventListener
    fun inviteReceived(event: InviteReceivedEvent) {
        websocketNotificationService.inviteReceived(
            userId = event.invitee.id,
            inviteId = event.inviteId,
            inviterUsername = event.inviter.username,
        )
    }

    @EventListener
    fun inviteAccepted(event: InviteAcceptedEvent) {
        websocketNotificationService.inviteAccepted(
            userId = event.inviter.id,
            inviteId = event.inviteId,
            inviteeUsername = event.invitee.username,
        )
    }

    @EventListener
    fun inviteRejected(event: InviteRejectedEvent) {
        websocketNotificationService.inviteRejected(
            userId = event.inviter.id,
            inviteId = event.inviteId,
            inviteeUsername = event.invitee.username,
        )
    }
}

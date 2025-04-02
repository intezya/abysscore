package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.event.matchinvite.InviteReceivedEvent
import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

class MatchInviteReceivedTests : BaseApiTest() {
    @Test
    fun `invitee should be notified that a match invite was received`() {
        val inviter = generateUserWithToken()
        val invitee = generateUserWithToken()

        WebSocketFixture.getSession(inviter.second, mainWebsocketUrl)
        val inviteeSession = WebSocketFixture.getSession(invitee.second, mainWebsocketUrl)

        publishInviteReceivedEvent(inviteId = 0L, inviter = inviter.first, invitee = invitee.first)

        val waitTimeoutSeconds = 1L
        var foundNotification = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = inviteeSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.MATCH_INVITE_RECEIVED)) {
                foundNotification = true
                break
            }
        }

        assertTrue(foundNotification)
    }

    private fun publishInviteReceivedEvent(inviteId: Long, inviter: User, invitee: User) {
        eventPublisher.publishEvent(
            InviteReceivedEvent(
                source = this,
                inviteId = inviteId,
                invitee = invitee,
                inviter = inviter,
            ),
        )
    }
}

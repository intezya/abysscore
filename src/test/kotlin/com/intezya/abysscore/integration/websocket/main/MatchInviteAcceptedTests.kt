package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.event.matchinvite.InviteAcceptedEvent
import com.intezya.abysscore.integration.controller.BaseApiTest
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import junit.framework.TestCase.assertTrue
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class MatchInviteAcceptedTests : BaseApiTest() {
    @Test
    fun `inviter should be notified that a match invite was accepted`() {
        val inviter = generateUserWithToken()
        val invitee = generateUserWithToken()

        val inviterSession = WebSocketFixture.getSession(inviter.second, mainWebsocketUrl)
        WebSocketFixture.getSession(invitee.second, mainWebsocketUrl)

        publishInviteAcceptedEvent(inviteId = 0L, inviter = inviter.first, invitee = invitee.first)

        val waitTimeoutSeconds = 1L
        var foundNotification = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = inviterSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.MATCH_INVITE_ACCEPTED)) {
                foundNotification = true
                break
            }
        }

        assertTrue(foundNotification)
    }

    private fun publishInviteAcceptedEvent(inviteId: Long, inviter: User, invitee: User) {
        eventPublisher.publishEvent(
            InviteAcceptedEvent(
                source = this,
                inviteId = inviteId,
                invitee = invitee,
                inviter = inviter,
            ),
        )
    }
}

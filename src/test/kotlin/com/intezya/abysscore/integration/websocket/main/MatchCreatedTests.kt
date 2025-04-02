package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class MatchCreatedTests : BaseApiTest() {
    @Test
    fun `should notify both users that match created`() {
        val user1 = generateUserWithToken()
        val user2 = generateUserWithToken()

        val user1Session = WebSocketFixture.getSession(user1.second, mainWebsocketUrl)
        val user2Session = WebSocketFixture.getSession(user2.second, mainWebsocketUrl)

        matchMakingService.createMatch(user1.first, user2.first)

        val waitTimeoutSeconds = 1L
        var foundNotification = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = user1Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.MATCH_CREATED)) {
                foundNotification = true
                break
            }
        }

        assertTrue(foundNotification)

        val waitTimeoutSeconds2 = 1L
        var foundNotification2 = false
        val startTime2 = System.nanoTime()
        val endTime2 = startTime2 + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds2)

        while (System.nanoTime() < endTime2) {
            val message = user2Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.MATCH_CREATED)) {
                foundNotification2 = true
                break
            }
        }

        assertTrue(foundNotification2)
    }
}

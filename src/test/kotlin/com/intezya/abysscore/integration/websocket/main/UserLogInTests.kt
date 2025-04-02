package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.RepeatedTest
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserLogInTests : BaseApiTest() {
    @RepeatedTest(10)
    fun `should notify users that user logged in`() {
        val primaryUser = generateUserWithToken()
        val otherUsers = List(4) { generateUserWithToken() }

        val otherSessions = otherUsers.map { userAndToken ->
            WebSocketFixture.getSession(userAndToken.second, mainWebsocketUrl)
        }

        WebSocketFixture.getSession(primaryUser.second, mainWebsocketUrl)

        val waitTimeoutSeconds = 1L

        otherSessions.forEach { otherSession ->
            val targetUsername = primaryUser.first.username
            var foundNotification = false
            val startTime = System.nanoTime()
            val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

            while (System.nanoTime() < endTime) {
                val message = otherSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

                if (message != null && message.contains(targetUsername) && message.contains("logged in")) {
                    foundNotification = true
                    break
                }
            }

            assertTrue(foundNotification, "Login notification for '$targetUsername' was not received")
        }
    }

    @RepeatedTest(5)
    fun `shouldn't notify yourself that logged in`() {
        val primaryUser = generateUserWithToken()
        val session = WebSocketFixture.getSession(primaryUser.second, mainWebsocketUrl)

        val waitTimeoutSeconds = 1L

        val targetUsername = primaryUser.first.username
        var foundNotification = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(targetUsername) && message.contains("logged in")) {
                foundNotification = true
                break
            }
        }

        assertFalse(foundNotification, "Login notification for '$targetUsername' was not received")
    }
}

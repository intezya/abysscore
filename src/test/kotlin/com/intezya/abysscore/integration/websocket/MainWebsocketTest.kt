package com.intezya.abysscore.integration.websocket

import com.intezya.abysscore.integration.controller.BaseApiTest
import com.intezya.abysscore.utils.fixtures.UserFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.*
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainWebsocketTest : BaseApiTest() {
    @LocalServerPort
    private var port: Int = 0

    private var mainWebsocketUrl = "ws://localhost:$port/hubs/main"

    @BeforeEach
    fun setupWebSocketUrl() {
        mainWebsocketUrl = "ws://localhost:$port/hubs/main"
        userRepository.deleteAll()
    }


    @Nested
    inner class WebsocketTests {
        @Test
        fun `should connect`() {
            WebSocketFixture.getSession(generateToken(), mainWebsocketUrl)
        }

        @Test
        fun `shouldn't connect if user not exists`() {
            val nonExistentUserToken = generateToken(UserFixtures.generateDefaultUser())

            assertThrows<IllegalStateException> {
                WebSocketFixture.getSession(nonExistentUserToken, mainWebsocketUrl)
            }
        }

        @Test
        fun `shouldn't connect to main websocket with empty token`() {
            assertThrows<IllegalStateException> {
                WebSocketFixture.getSession("", mainWebsocketUrl)
            }
        }

        @Test
        fun `shouldn't connect without authorization header`() {
            val latch = CountDownLatch(1)
            val receivedMessages = mutableListOf<String>()

            val clientHandler = object : TextWebSocketHandler() {
                override fun handleTextMessage(
                    session: WebSocketSession,
                    message: TextMessage,
                ) {
                    receivedMessages.add(message.payload)
                    latch.countDown()
                }
            }

            val headers = WebSocketHttpHeaders()

            assertThrows<ExecutionException> {
                StandardWebSocketClient().execute(clientHandler, headers, URI(mainWebsocketUrl)).get()
            }
        }
    }

    @Nested
    inner class UserLogInTests {
        @RepeatedTest(1000)
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

        //        @Test
        @RepeatedTest(100)
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

    @Nested
    inner class UserLogOutTests {
        @RepeatedTest(1000)
        fun `should notify users that user logged out`() {
            val primaryUser = generateUserWithToken()
            val otherUsers = List(4) { generateUserWithToken() }

            val otherSessions = otherUsers.map { userAndToken ->
                WebSocketFixture.getSession(userAndToken.second, mainWebsocketUrl)
            }

            val primaryUserSession = WebSocketFixture.getSession(primaryUser.second, mainWebsocketUrl)
            primaryUserSession.session.close()

            val waitTimeoutSeconds = 1L

            otherSessions.forEach { otherSession ->
                val targetUsername = primaryUser.first.username
                var foundNotification = false
                val startTime = System.nanoTime()
                val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

                while (System.nanoTime() < endTime) {
                    val message = otherSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

                    if (message != null && message.contains(targetUsername) && message.contains("logged out")) {
                        foundNotification = true
                        break
                    }
                }

                assertTrue(foundNotification, "Logout notification for '$targetUsername' was not received")
            }
        }
    }
}

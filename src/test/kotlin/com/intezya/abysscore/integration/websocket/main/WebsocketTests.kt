package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.controller.BaseApiTest
import com.intezya.abysscore.utils.fixtures.UserFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.assertThrows
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import kotlin.test.Test

class WebsocketTests : BaseApiTest() {
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
            override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
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

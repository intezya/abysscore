package com.intezya.abysscore.utils.fixtures

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object WebSocketFixture {
    fun getSession(
        authToken: String,
        uri: String,
    ): ProvidedSession {
        val messageQueue: BlockingQueue<String> = LinkedBlockingQueue()

        val clientHandler = object : TextWebSocketHandler() {
            override fun handleTextMessage(
                session: WebSocketSession,
                message: TextMessage,
            ) {
                try {
                    messageQueue.put(message.payload)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    System.err.println("Client handler interrupted while adding message to queue: ${e.message}")
                } catch (e: Exception) {
                    System.err.println("Error handling message in client: ${e.message}")
                }
            }

            override fun afterConnectionEstablished(session: WebSocketSession) {
                System.out.println("DEBUG Client ${session.id}: Connection established.")
            }

            override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
                System.err.println("ERROR Client ${session.id}: Transport error: ${exception.message}")
                exception.printStackTrace()
            }

            override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
                System.out.println("DEBUG Client ${session.id}: Connection closed: $status")
            }
        }

        val headers = WebSocketHttpHeaders()
        headers.set("Authorization", "Bearer $authToken")

        try {
            val session = StandardWebSocketClient()
                .execute(clientHandler, headers, URI(uri))
                .get(5, TimeUnit.SECONDS)

            return ProvidedSession(
                messageQueue = messageQueue,
                session = session,
            )
        } catch (e: Exception) {
            System.err.println("Failed to establish WebSocket connection for token starting with ${authToken.take(5)}...: ${e.message}")
            throw IllegalStateException("Failed to establish WebSocket connection", e)
        }
    }

    data class ProvidedSession(
        val messageQueue: BlockingQueue<String>,
        val session: WebSocketSession,
    )
}

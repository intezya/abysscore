package com.intezya.abysscore.integration.websocket.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.model.message.websocket.draft.process.CharactersRevealMessage
import com.intezya.abysscore.utils.fixtures.DraftCharactersFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class DraftCharactersRevealTests : BaseApiTest() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `must notify opponent that characters revealed`() {
        val sender = generateUserWithToken()
        val receiver = generateUserWithToken()

        val receiverSession = WebSocketFixture.getSession(receiver.second, mainWebsocketUrl)

        matchMakingService.createMatch(sender.first, receiver.first)
        val characters = DraftCharactersFixtures.createDraftCharacters(200)

        draftCharacterRevealService.revealCharacters(sender.first, characters)

        val waitTimeoutSeconds = 5L
        var foundNotification = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = receiverSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (verifyMessage(message, characters)) {
                foundNotification = true
                break
            }
        }

        assertTrue(foundNotification)
    }

    private fun verifyMessage(message: String?, characters: List<DraftCharacterDTO>): Boolean = try {
        message != null &&
            message.contains(Messages.DRAFT_CHARACTERS_REVEAL) &&
            objectMapper.readValue(message, CharactersRevealMessage::class.java).characters == characters
    } catch (_: Exception) {
        false
    }
}

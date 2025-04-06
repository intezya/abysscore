package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.DraftCharactersFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import kotlin.test.Test

class DraftPlayersReadyTests : BaseApiTest() {
    @Test
    fun `should notify draft websocket about draft end after all actions`() {
        val player1 = generateUserWithToken()
        val player2 = generateUserWithToken()

        val player1Session = WebSocketFixture.getSession(player1.second, draftWebsocketUrl)
        val player2Session = WebSocketFixture.getSession(player2.second, draftWebsocketUrl)

        matchMakingService.createMatch(player1.first, player2.first)

        val characters1 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt() (can cause exception)
        val characters2 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt()

        draftCharacterRevealService.revealCharacters(player1.first, characters1)
        draftCharacterRevealService.revealCharacters(player2.first, characters2)

        draftCharacterRevealService.readyForDraft(player1.first)
        draftCharacterRevealService.readyForDraft(player2.first)

        checkNotification(player1Session, Messages.DRAFT_BOTH_PLAYERS_READY)
        checkNotification(player2Session, Messages.DRAFT_BOTH_PLAYERS_READY)
    }
}

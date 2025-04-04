package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.service.MatchProcessService
import com.intezya.abysscore.utils.fixtures.MatchFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test
import kotlin.test.assertTrue

class MatchProcessTests : BaseApiTest() {
    @Autowired
    private lateinit var matchProcessService: MatchProcessService

    // TODO: test (not here) that match moved to next stage
    @Test
    fun `should notify that opponent submitted his result`() {
        val player1 = generateUserWithToken()
        val player2 = generateUserWithToken()
        val player1Session = WebSocketFixture.getSession(player1.second, mainWebsocketUrl)
        val player2Session = WebSocketFixture.getSession(player2.second, mainWebsocketUrl)
        val match = matchMakingService.createMatch(player1.first, player2.first)
        match.status = MatchStatus.ACTIVE
        matchRepository.save(match)

        checkPlayerSubmits(player1.first, player2Session)
        checkPlayerSubmits(player2.first, player1Session)
    }

    private fun checkPlayerSubmits(player: User, opponentSession: WebSocketFixture.ProvidedSession) {
        for (roomNumber in 1..3) {
            val result = MatchFixtures.createSubmitResult(roomNumber = roomNumber)
            matchProcessService.submitResult(player, result)
            assertTrue(
                checkNotification(
                    session = opponentSession,
                    Messages.MATCH_RESULT_SUBMIT,
                    result.roomNumber.toString(),
                    result.time.toString(),
                ),
                "Notification was not received for room $roomNumber",
            )
        }
    }
}

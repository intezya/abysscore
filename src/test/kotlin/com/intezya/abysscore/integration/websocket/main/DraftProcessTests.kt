package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.DraftCharactersFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import java.util.concurrent.TimeUnit

class DraftProcessTests : BaseApiTest() {
    //    @Test
    // TODO: need detailed test
    @RepeatedTest(10)
    fun `should notify about draft action`() {
        val player1 = generateUserWithToken()
        val player2 = generateUserWithToken()

        val player1Session = WebSocketFixture.getSession(player1.second, draftWebsocketUrl)
        val player2Session = WebSocketFixture.getSession(player2.second, draftWebsocketUrl)

        val match = matchMakingService.createMatch(player1.first, player2.first)

        val characters1 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt() (can cause exception)
        val characters2 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt()

        draftCharacterRevealService.revealCharacters(player1.first, characters1)
        draftCharacterRevealService.revealCharacters(player2.first, characters2)

        // TODO: add test that forbid any action (that not reveal) if players not ready

        val draft = match.draft

        // TODO: test completion system; test match, draft status
        while (draft.getCurrentStep() != null) {
            val currentStep = draft.getCurrentStep()
            val currentStepPlayer = if (currentStep!!.firstPlayer) player1.first else player2.first

            val availableCharacters = when {
                currentStep.isPick && currentStep.firstPlayer -> draft.player1AvailableCharacters
                currentStep.isPick && !currentStep.firstPlayer -> draft.player2AvailableCharacters
                !currentStep.isPick && currentStep.firstPlayer -> draft.player2AvailableCharacters
                else -> draft.player1AvailableCharacters
            }

            // TODO: test case when 1playerCharacters.size < draft schema size
            val randomCharacter = availableCharacters.random()

            draftActionService.performDraftAction(
                user = currentStepPlayer,
                characterName = randomCharacter.name,
            )
        }

        // TODO:  test that draft ended and match moved to next state

        val stepsCount = draft.getDraftSteps().size

        for (i in 0 until stepsCount.div(2)) {
            val waitTimeoutSeconds = 1L
            var foundNotification = false
            val startTime = System.nanoTime()
            val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

            while (System.nanoTime() < endTime) {
                val message = player1Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

                if (message != null && message.contains(Messages.DRAFT_ACTION_PERFORMED)) {
                    foundNotification = true
                    break
                }
            }

            assertTrue(foundNotification)
        }

        for (i in 0 until stepsCount.div(2)) {
            val waitTimeoutSeconds = 1L
            var foundNotification = false
            val startTime = System.nanoTime()
            val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

            while (System.nanoTime() < endTime) {
                val message = player2Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

                if (message != null && message.contains(Messages.DRAFT_ACTION_PERFORMED)) {
                    foundNotification = true
                    break
                }
            }

            assertTrue(foundNotification)
        }
    }
}

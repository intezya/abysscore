package com.intezya.abysscore.integration.websocket.main

import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.message.websocket.Messages
import com.intezya.abysscore.utils.fixtures.DraftCharactersFixtures
import com.intezya.abysscore.utils.fixtures.WebSocketFixture
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class DraftCompletionServiceTests : BaseApiTest() {
    // TODO: need detailed test
    // TODO: simplify test flow
    @Test
    fun `should notify draft websocket about draft end after all actions`() {
        val player1 = generateUserWithToken()
        val player2 = generateUserWithToken()

        val player1Session = WebSocketFixture.getSession(player1.second, draftWebsocketUrl)
        val player2Session = WebSocketFixture.getSession(player2.second, draftWebsocketUrl)

        val match = matchMakingService.createMatch(player1.first, player2.first)

        val characters1 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt() (can cause exception)
        val characters2 = DraftCharactersFixtures.createDraftCharacters(4) // Random.nextInt()

        draftCharacterRevealService.revealCharacters(player1.first, characters1)
        draftCharacterRevealService.revealCharacters(player2.first, characters2)

        val draft = match.draft

        while (draft.getCurrentStep() != null) {
            val currentStep = draft.getCurrentStep()
            val currentStepPlayer = if (currentStep!!.firstPlayer) player1.first else player2.first

            val availableCharacters = when {
                currentStep.isPick && currentStep.firstPlayer -> draft.player1AvailableCharacters
                currentStep.isPick && !currentStep.firstPlayer -> draft.player2AvailableCharacters
                !currentStep.isPick && currentStep.firstPlayer -> draft.player2AvailableCharacters
                else -> draft.player1AvailableCharacters
            }

            val randomCharacter = availableCharacters.random()

            draftActionService.performDraftAction(
                user = currentStepPlayer,
                characterName = randomCharacter,
            )
        }

        val stepsCount = draft.stepsSize

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

        val waitTimeoutSeconds = 1L

        var foundEndNotificationPlayer1 = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = player1Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.DRAFT_END)) {
                foundEndNotificationPlayer1 = true
                break
            }
        }

        assertTrue(foundEndNotificationPlayer1)

        var foundEndNotificationPlayer2 = false

        while (System.nanoTime() < endTime) {
            val message = player2Session.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.DRAFT_END)) {
                foundEndNotificationPlayer2 = true
                break
            }
        }

        assertTrue(foundEndNotificationPlayer2)
    }

    // TODO: simplify test flow
    @Test
    fun `should notify main websocket about draft end after all actions`() {
        val player1 = generateUserWithToken()
        val player2 = generateUserWithToken()

        val player1MainSession = WebSocketFixture.getSession(player1.second, mainWebsocketUrl)
        val player2MainSession = WebSocketFixture.getSession(player2.second, mainWebsocketUrl)

        val player1DraftSession = WebSocketFixture.getSession(player1.second, draftWebsocketUrl)
        val player2DraftSession = WebSocketFixture.getSession(player2.second, draftWebsocketUrl)

        val match = matchMakingService.createMatch(player1.first, player2.first)

        val characters1 = DraftCharactersFixtures.createDraftCharacters(40) // Random.nextInt() (can cause exception)
        val characters2 = DraftCharactersFixtures.createDraftCharacters(40) // Random.nextInt()

        draftCharacterRevealService.revealCharacters(player1.first, characters1)
        draftCharacterRevealService.revealCharacters(player2.first, characters2)

        val draft = match.draft

        draftCharacterRevealService.readyForDraft(player1.first)
        draftCharacterRevealService.readyForDraft(player2.first)

        while (draft.getCurrentStep() != null) {
            val currentStep = draft.getCurrentStep()
            val currentStepPlayer = if (currentStep!!.firstPlayer) player1.first else player2.first

            val availableCharacters = when {
                currentStep.isPick && currentStep.firstPlayer -> draft.player1AvailableCharacters
                currentStep.isPick && !currentStep.firstPlayer -> draft.player2AvailableCharacters
                !currentStep.isPick && currentStep.firstPlayer -> draft.player2AvailableCharacters
                else -> draft.player1AvailableCharacters
            }

            val randomCharacter = availableCharacters.random()

            draftActionService.performDraftAction(
                user = currentStepPlayer,
                characterName = randomCharacter,
            )
        }

        val stepsCount = draft.stepsSize

        for (i in 0 until stepsCount.div(2)) {
            val waitTimeoutSeconds = 1L
            var foundNotification = false
            val startTime = System.nanoTime()
            val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

            while (System.nanoTime() < endTime) {
                val message = player1DraftSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

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
                val message = player2DraftSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

                if (message != null && message.contains(Messages.DRAFT_ACTION_PERFORMED)) {
                    foundNotification = true
                    break
                }
            }

            assertTrue(foundNotification)
        }

        val waitTimeoutSeconds = 1L

        var foundEndNotificationPlayer1 = false
        val startTime = System.nanoTime()
        val endTime = startTime + TimeUnit.SECONDS.toNanos(waitTimeoutSeconds)

        while (System.nanoTime() < endTime) {
            val message = player1MainSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.DRAFT_END)) {
                foundEndNotificationPlayer1 = true
                break
            }
        }

        assertTrue(foundEndNotificationPlayer1)

        var foundEndNotificationPlayer2 = false

        while (System.nanoTime() < endTime) {
            val message = player2MainSession.messageQueue.poll(250, TimeUnit.MILLISECONDS)

            if (message != null && message.contains(Messages.DRAFT_END)) {
                foundEndNotificationPlayer2 = true
                break
            }
        }

        assertTrue(foundEndNotificationPlayer2)
    }
}

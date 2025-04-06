package com.intezya.abysscore.listener

import com.intezya.abysscore.event.draftprocess.*
import com.intezya.abysscore.model.dto.draft.toDTO
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DraftProcessEventListener(private val websocketNotificationService: WebsocketNotificationService) {
    @EventListener
    fun charactersReveal(event: CharactersRevealEvent) {
        val opponent = event.match.getOpponent(event.player)

        websocketNotificationService.charactersRevealed(
            opponentId = opponent.id,
            characters = event.characters,
        )
    }

    @EventListener
    fun draftActionPerform(event: DraftActionPerformEvent) {
        val opponent = event.match.getOpponent(event.player)

        websocketNotificationService.draftActionPerform(
            opponentId = opponent.id,
            draftAction = event.action.toDTO(),
        )
    }

    // TODO: add unit test
    @EventListener
    fun draftEnd(event: DraftEndEvent) {
        // TODO: move filter&map draft as function
        val draft = event.match.draft
        val player1CharactersNames = draft.draftActions.filter {
            it.player == event.match.player1
            it.isPick == true
        }.map { it.characterName }
        val player2CharactersNames = draft.draftActions.filter {
            it.player == event.match.player2
            it.isPick == true
        }.map { it.characterName }

        val player1Characters = draft.player1Characters
            .filter { it.name in player1CharactersNames }
        val player2Characters = draft.player2Characters
            .filter { it.name in player2CharactersNames }

        websocketNotificationService.sendDraftEnd(
            player1Id = event.match.player1.id,
            player2Id = event.match.player2.id,
            player1Characters = player1Characters.map { it.name },
            player2Characters = player2Characters.map { it.name },
        )
    }

    @EventListener
    fun onAutomaticDraftActionPerform(event: AutomaticDraftActionPerformEvent) {
        websocketNotificationService.automaticDraftActionPerform(
            event.match.player1.id,
            event.match.player2.id,
            event.action.toDTO(),
        )
    }

    @EventListener
    fun onBothPlayersReady(event: BothPlayersReadyEvent) {
        websocketNotificationService.sendBothPlayersReadyForDraft(
            event.match.player1.id,
            event.match.player2.id,
            event.match.draft.toDTO(),
        )
    }
}

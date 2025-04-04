package com.intezya.abysscore.listener

import com.intezya.abysscore.event.draftprocess.AutomaticDraftActionPerformEvent
import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.event.draftprocess.DraftActionPerformEvent
import com.intezya.abysscore.event.draftprocess.DraftEndEvent
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
        val player1CharactersDTO = event.draft.player1AvailableCharacters
            .filter {
                event.draft.player1Characters.contains(it.name)
            }
            .map { it.toDTO() }
        val player2CharactersDTO = event.draft.player2AvailableCharacters
            .filter {
                event.draft.player2Characters.contains(it.name)
            }
            .map { it.toDTO() }

        websocketNotificationService.sendDraftEnd(
            player1Id = event.match.player1.id,
            player2Id = event.match.player2.id,
            player1Characters = player1CharactersDTO,
            player2Characters = player2CharactersDTO,
        )
    }

    @EventListener
    fun onAutomaticDraftActionPerform(event: AutomaticDraftActionPerformEvent) {
        websocketNotificationService.automaticDraftActionPerform(event.match.player1.id, event.action.toDTO())
        websocketNotificationService.automaticDraftActionPerform(event.match.player2.id, event.action.toDTO())
    }
}

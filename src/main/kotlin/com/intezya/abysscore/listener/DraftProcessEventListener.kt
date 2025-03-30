package com.intezya.abysscore.listener

import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.event.draftprocess.DraftActionPerformEvent
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
}

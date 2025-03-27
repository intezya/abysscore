package com.intezya.abysscore.listener

import com.intezya.abysscore.event.matchmaking.MatchCreatedEvent
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchMakingEventListener(private val websocketNotificationService: WebsocketNotificationService) {
    @EventListener
    fun matchCreated(event: MatchCreatedEvent) {
        websocketNotificationService.matchCreated(
            userId = event.match.player1.id,
            matchId = event.match.id,
            opponent = event.match.player2.toDTO(),
        )

        websocketNotificationService.matchCreated(
            userId = event.match.player2.id,
            matchId = event.match.id,
            opponent = event.match.player1.toDTO(),
        )
    }
}

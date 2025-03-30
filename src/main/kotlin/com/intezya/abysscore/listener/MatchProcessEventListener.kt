package com.intezya.abysscore.listener

import com.intezya.abysscore.enum.TimeoutResult
import com.intezya.abysscore.event.matchprocess.MatchTimeoutEvent
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchProcessEventListener(
    private val websocketNotificationService: WebsocketNotificationService,
) {
    @EventListener
    fun onMatchTimeout(event: MatchTimeoutEvent) {
        val player1 = event.match.player1
        val player2 = event.match.player2

        when (event.timeoutResult) {
            TimeoutResult.BOTH_TIMEOUT -> {
                websocketNotificationService.sendTimeoutDraw(player1.id, player2.id, event.match.id)
            }

            TimeoutResult.PLAYER1_TIMEOUT -> {
                websocketNotificationService.sendTimeoutDefeat(player1.id, event.match.id)
                websocketNotificationService.sendTimeoutVictory(player2.id, event.match.id)
            }

            TimeoutResult.PLAYER2_TIMEOUT -> {
                websocketNotificationService.sendTimeoutVictory(player1.id, event.match.id)
                websocketNotificationService.sendTimeoutDefeat(player2.id, event.match.id)
            }
        }
    }
}

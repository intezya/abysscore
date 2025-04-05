package com.intezya.abysscore.listener

import com.intezya.abysscore.enum.TimeoutResult
import com.intezya.abysscore.event.match.process.MatchEndEvent
import com.intezya.abysscore.event.match.process.MatchSubmitResultEvent
import com.intezya.abysscore.event.match.process.MatchTimeoutEvent
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchProcessEventListener(private val websocketNotificationService: WebsocketNotificationService) {
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

    @EventListener
    fun onMatchSubmitResult(event: MatchSubmitResultEvent) {
        val player = event.result.player
        val opponent = event.match.getOpponent(player)

        websocketNotificationService.sendSubmitResult(
            opponentId = opponent.id,
            roomNumber = event.result.roomNumber,
            result = event.result.time,
        )
    }

    @EventListener
    fun onMatchEnd(event: MatchEndEvent) {
        val player1 = event.match.player1
        val player2 = event.match.player2

        event.match.winner

        val player1Result = event.match.determineResultForPlayer(player1)
        val player2Result = event.match.determineResultForPlayer(player2)

        websocketNotificationService.sendMatchEnd(
            playerId = player1.id,
            playerScore = event.player1Score,
            opponentScore = event.player2Score,
            thisPlayerWinner = player1Result,
        )

        websocketNotificationService.sendMatchEnd(
            playerId = player2.id,
            playerScore = event.player2Score,
            opponentScore = event.player1Score,
            thisPlayerWinner = player2Result,
        )
    }
}

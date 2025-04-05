package com.intezya.abysscore.service.match

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.match.process.MatchEndEvent
import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class MatchProcessService(
    private val matchResultService: MatchResultService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    class MatchCompletionCheckEvent(source: Any, val match: Match) : ApplicationEvent(source)

    fun submitRetry(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatchOrThrow()
        return matchResultService.processRetry(user, currentMatch, request)
    }

    fun submitResult(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatchOrThrow()
        matchResultService.processResult(user, currentMatch, request)
        eventPublisher.publishEvent(MatchCompletionCheckEvent(this, currentMatch))
        return currentMatch
    }

    @EventListener
    fun handleMatchCompletion(event: MatchCompletionCheckEvent) {
        val match = event.match

        if (match.isRoomResultsFilled() && match.status == MatchStatus.ACTIVE) {
            match.endedAt = LocalDateTime.now()
            match.status = MatchStatus.COMPLETED
            val player1Score = match.getPlayerScore(match.player1)
            val player2Score = match.getPlayerScore(match.player2)

            when {
                player1Score < player2Score -> match.winner = match.player1
                player1Score > player2Score -> match.winner = match.player2
                else -> match.status = MatchStatus.DRAW
            }

            matchResultService.saveMatch(match)

            eventPublisher.publishEvent(MatchEndEvent(this, match, player1Score, player2Score))

            // TODO: update statistics
        }
    }

    // TODO in draft services
    private fun User.currentMatchOrThrow(): Match =
        currentMatch ?: throw IllegalStateException("User is not in a match")
}

package com.intezya.abysscore.service.match

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.enum.TimeoutResult
import com.intezya.abysscore.event.match.process.MatchTimeoutEvent
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchRepository
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

private const val MATCH_TIMEOUT_CHECK_INTERVAL_MS = 30 * 1000L // 30 seconds

@Service
@Transactional
class MatchTimeoutService(
    private val matchRepository: MatchRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    @Scheduled(fixedRate = MATCH_TIMEOUT_CHECK_INTERVAL_MS)
    fun checkMatchTimeouts() {
        logger.debug("Starting match timeout check")

        val activeMatches = matchRepository.findByStatus(MatchStatus.ACTIVE)
        logger.debug("Processing ${activeMatches.size} active matches for timeouts")

        activeMatches.forEach { match ->
            try {
                processMatchTimeout(match)
            } catch (e: Exception) {
                logger.error("Error processing timeout for match ${match.id}", e)
            }
        }

        logger.debug("Completed match timeout check")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun processMatchTimeout(match: Match) {
        val timeoutThreshold = match.status.timeout
        checkPlayerTimeouts(match, timeoutThreshold)
    }

    fun checkPlayerTimeouts(
        match: Match,
        timeoutThreshold: Duration,
        now: LocalDateTime = LocalDateTime.now(),
        playerResults: Map<User, LocalDateTime> = getLastResultsForPlayers(match, now),
    ) {
        val player1 = match.player1
        val player2 = match.player2

        val player1LastResult = playerResults[player1] ?: match.startedAt
        val player2LastResult = playerResults[player2] ?: match.startedAt

        val player1Inactivity = Duration.between(player1LastResult, now)
        val player2Inactivity = Duration.between(player2LastResult, now)

        logger.debug(
            "Match ${match.id}: Player1 inactivity: ${player1Inactivity.toMinutes()} min, " +
                "Player2 inactivity: ${player2Inactivity.toMinutes()} min, " +
                "Threshold: ${timeoutThreshold.toMinutes()} min",
        )

        when {
            player1Inactivity > timeoutThreshold && player2Inactivity > timeoutThreshold ->
                handleBothPlayersTimeout(match, player1Inactivity, player2Inactivity)

            player1Inactivity > timeoutThreshold ->
                assignTechnicalDefeat(
                    match,
                    player1,
                    "Timeout exceeded",
                    player1Inactivity,
                    player2Inactivity,
                )

            player2Inactivity > timeoutThreshold ->
                assignTechnicalDefeat(
                    match,
                    player2,
                    "Timeout exceeded",
                    player1Inactivity,
                    player2Inactivity,
                )
        }
    }

    private fun getLastResultsForPlayers(match: Match, now: LocalDateTime): Map<User, LocalDateTime> {
        val playerMap = mapOf(match.player1.id to match.player1, match.player2.id to match.player2)
        val playerIds = playerMap.keys

        val allActions = match.roomResults.map { it.player.id to it.completedAt } +
            match.roomRetries.map { it.player.id to it.completedAt }

        return allActions
            .filter { (playerId, _) -> playerId in playerIds }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, timestamps) -> timestamps.maxOrNull() ?: now }
            .mapKeys { (playerId, _) -> playerMap[playerId]!! }
    }

    private fun handleBothPlayersTimeout(match: Match, player1Inactivity: Duration, player2Inactivity: Duration) {
        when {
            player1Inactivity < player2Inactivity ->
                assignTechnicalDefeat(
                    match,
                    match.player2,
                    "Longer inactivity compared to opponent",
                    player1Inactivity,
                    player2Inactivity,
                )

            player2Inactivity < player1Inactivity ->
                assignTechnicalDefeat(
                    match,
                    match.player1,
                    "Longer inactivity compared to opponent",
                    player1Inactivity,
                    player2Inactivity,
                )

            else -> declareMatchAsDraw(match)
        }

        eventPublisher.publishEvent(
            MatchTimeoutEvent(
                this,
                match,
                TimeoutResult.BOTH_TIMEOUT,
                player1Inactivity,
                player2Inactivity,
            ),
        )
    }

    private fun declareMatchAsDraw(match: Match) {
        endMatch(
            match = match,
            status = MatchStatus.DRAW,
            winner = null,
            reason = "Both players timed out with equal inactivity",
        )
        logger.info("Match ${match.id} ended in draw due to both players timing out equally")
        // TODO
//        notificationService.notifyPlayersAboutDraw(match)
    }

    private fun assignTechnicalDefeat(
        match: Match,
        timeoutPlayer: User,
        reason: String,
        player1Inactivity: Duration,
        player2Inactivity: Duration,
    ) {
        val winner = if (timeoutPlayer == match.player1) match.player2 else match.player1

        endMatch(
            match = match,
            status = MatchStatus.COMPLETED,
            winner = winner,
            reason = reason,
        )

        logger.info("Technical defeat assigned to player ${timeoutPlayer.id} in match ${match.id} due to $reason")
        // TODO
//        notificationService.notifyPlayerAboutTechnicalDefeat(match, timeoutPlayer, reason)
//        notificationService.notifyPlayerAboutTechnicalWin(match, winner)

        eventPublisher.publishEvent(
            MatchTimeoutEvent(
                this,
                match,
                if (timeoutPlayer == match.player1) TimeoutResult.PLAYER1_TIMEOUT else TimeoutResult.PLAYER2_TIMEOUT,
                player1Inactivity,
                player2Inactivity,
            ),
        )
    }

    internal fun endMatch(match: Match, status: MatchStatus, winner: User?, reason: String) {
        match.apply {
            this.status = status
            this.endedAt = LocalDateTime.now()
            this.winner = winner
            this.technicalDefeatReason = reason

            player1.currentMatch = null
            player2.currentMatch = null
        }
        matchRepository.save(match)

        // TODO: notify
    }
}

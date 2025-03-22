package com.intezya.abysscore.service

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.RoomResult
import com.intezya.abysscore.model.entity.RoomRetry
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.MatchRepository
import com.intezya.abysscore.repository.RoomResultRepository
import com.intezya.abysscore.repository.RoomRetryRepository
import org.apache.commons.logging.LogFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime

private const val MAX_RETRIES_COUNT = 5
private const val RETRY_PENALTY = 5
private const val MATCH_TIMEOUT_CHECK_INTERVAL_MS = 30 * 1000L // 30 seconds

@Service
@Transactional
class MatchProcessService(
    private val roomResultRepository: RoomResultRepository,
    private val roomRetryRepository: RoomRetryRepository,
    private val matchRepository: MatchRepository,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun submitRetry(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatchOrThrow()
        validateMatchIsActive(currentMatch)
        validateRetryLimits(user, currentMatch)

        val roomRetry = createRoomRetry(user, currentMatch, request)
        saveAndAddRetry(currentMatch, roomRetry)

        return currentMatch
    }

    fun submitResult(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatchOrThrow()
        validateMatchIsActive(currentMatch)

        val penalty = calculatePenalty(user, currentMatch, request.roomNumber)
        val roomResult = createRoomResult(user, currentMatch, request, penalty)

        try {
            val savedResult = roomResultRepository.save(roomResult)
            currentMatch.roomResults.add(savedResult)
            handleMatchCompletion(currentMatch)
            return currentMatch
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already submitted result to this room",
            )
        }
    }

    @Scheduled(fixedRate = MATCH_TIMEOUT_CHECK_INTERVAL_MS)
    fun checkMatchTimeouts() {
        logger.info("Starting match timeout check")

        val activeMatches = matchRepository.findByStatus(MatchStatus.ACTIVE)
        logger.info("Processing ${activeMatches.size} active matches for timeouts")

        activeMatches.forEach { match ->
            try {
                processMatchTimeout(match)
            } catch (e: Exception) {
                logger.error("Error processing timeout for match ${match.id}", e)
            }
        }

        logger.info("Completed match timeout check")
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
                assignTechnicalDefeat(match, player1, "Timeout exceeded")

            player2Inactivity > timeoutThreshold ->
                assignTechnicalDefeat(match, player2, "Timeout exceeded")
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
                assignTechnicalDefeat(match, match.player2, "Longer inactivity compared to opponent")

            player2Inactivity < player1Inactivity ->
                assignTechnicalDefeat(match, match.player1, "Longer inactivity compared to opponent")

            else -> declareMatchAsDraw(match)
        }
    }

    private fun declareMatchAsDraw(match: Match) {
        endMatch(
            match = match,
            status = MatchStatus.DRAW,
            winner = null,
            reason = "Both players timed out with equal inactivity",
        )
        logger.info("Match ${match.id} ended in draw due to both players timing out equally")
        notifyPlayersAboutDraw(match)
    }

    private fun assignTechnicalDefeat(match: Match, timeoutPlayer: User, reason: String) {
        val winner = if (timeoutPlayer == match.player1) match.player2 else match.player1

        endMatch(
            match = match,
            status = MatchStatus.COMPLETED,
            winner = winner,
            reason = reason,
        )

        logger.info("Technical defeat assigned to player ${timeoutPlayer.id} in match ${match.id} due to $reason")
        notifyPlayerAboutTechnicalDefeat(match, timeoutPlayer, reason)
        notifyPlayerAboutTechnicalWin(match, winner)
    }

    private fun endMatch(match: Match, status: MatchStatus, winner: User?, reason: String) {
        match.apply {
            this.status = status
            this.endedAt = LocalDateTime.now()
            this.winner = winner
            this.technicalDefeatReason = reason

            player1.currentMatch = null
            player2.currentMatch = null
        }
        matchRepository.save(match)
    }

    private fun notifyPlayersAboutDraw(match: Match) {
        logger.debug("Notifying players about draw in match ${match.id}")
        // TODO: implement notification sending
    }

    private fun notifyPlayerAboutTechnicalDefeat(match: Match, player: User, reason: String) {
        logger.debug("Notifying player ${player.id} about technical defeat in match ${match.id}")
        // TODO: implement notification sending
    }

    private fun notifyPlayerAboutTechnicalWin(match: Match, player: User) {
        logger.debug("Notifying player ${player.id} about technical win in match ${match.id}")
        // TODO: implement notification sending
    }

    private fun validateRetryLimits(user: User, match: Match) {
        val retries = match.roomRetries.count { it.player == user }
        if (retries >= MAX_RETRIES_COUNT) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already submitted too many retries",
            )
        }
    }

    private fun createRoomRetry(user: User, match: Match, request: SubmitRoomResultRequest): RoomRetry = RoomRetry(
        roomNumber = request.roomNumber,
        time = request.time,
    ).apply {
        this.player = user
        this.match = match
    }

    private fun saveAndAddRetry(match: Match, roomRetry: RoomRetry) {
        val savedRetry = roomRetryRepository.save(roomRetry)
        match.roomRetries.add(savedRetry)
    }

    private fun calculatePenalty(user: User, match: Match, roomNumber: Int): Int {
        val usedRetries = roomRetryRepository.countByPlayerAndMatchAndRoomNumber(
            user,
            match,
            roomNumber,
        )
        return processPenaltyTime(usedRetries)
    }

    private fun createRoomResult(user: User, match: Match, request: SubmitRoomResultRequest, penalty: Int): RoomResult =
        RoomResult(
            roomNumber = request.roomNumber,
            time = request.time + penalty,
        ).apply {
            this.player = user
            this.match = match
        }

    private fun handleMatchCompletion(match: Match) {
        if (match.isEnded()) {
            processMatchEnd(match)
        }
    }

    private fun processMatchEnd(match: Match) {
        match.endedAt = LocalDateTime.now()
        matchRepository.save(match)
        // TODO: calculations, statistics update, notifications
    }

    private fun processPenaltyTime(retriesCount: Long): Int = when (retriesCount) {
        in 4L..5L -> RETRY_PENALTY
        else -> 0
    }

    private fun User.currentMatchOrThrow(): Match =
        currentMatch ?: throw IllegalStateException("User is not in a match")

    private fun validateMatchIsActive(match: Match) {
        if (match.status != MatchStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Match is not in active stage")
        }
    }
}

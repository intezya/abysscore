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
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime

private const val MAX_RETRIES_COUNT = 5
private const val RETRY_PENALTY = 5
private const val MATCH_TIMEOUT_CHECK_INTERVAL_MS = 30 * 1000L // 30 seconds
private const val MATCH_TIMEOUT_THRESHOLD_MS = 15 * 60 * 1000L // 15 minutes

@Service
@Transactional
class MatchProcessService(
    private val roomResultRepository: RoomResultRepository,
    private val roomRetryRepository: RoomRetryRepository,
    private val matchRepository: MatchRepository,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun submitRetry(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatch ?: throw IllegalStateException("User is not in a match")

        validateRetryLimits(user, currentMatch)

        val roomRetry = createRoomRetry(user, currentMatch, request)
        saveAndAddRetry(currentMatch, roomRetry)

        return currentMatch
    }

    fun submitResult(user: User, request: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatch ?: throw IllegalStateException("User is not in a match")
        val penalty = calculatePenalty(user, currentMatch, request.roomNumber)

        val roomResult = createRoomResult(user, currentMatch, request, penalty)
        val savedResult = saveRoomResult(roomResult)

        currentMatch.roomResults.add(savedResult)

        handleMatchCompletion(currentMatch)

        return currentMatch
    }

    @Scheduled(fixedRate = MATCH_TIMEOUT_CHECK_INTERVAL_MS)
    fun checkMatchTimeouts() {
        val activeMatches = matchRepository.findByStatus(MatchStatus.ACTIVE)

        activeMatches.forEach { match ->
            checkPlayerTimeouts(match)
        }
    }

    private fun checkPlayerTimeouts(match: Match) {
        val now = LocalDateTime.now()
        val timeoutThreshold = Duration.ofMillis(MATCH_TIMEOUT_THRESHOLD_MS)

        val player1LastResult = getLastResultOrStartTime(match, match.player1)
        val player2LastResult = getLastResultOrStartTime(match, match.player2)

        val player1Inactivity = Duration.between(player1LastResult, now)
        val player2Inactivity = Duration.between(player2LastResult, now)

        if (player1Inactivity > timeoutThreshold && player2Inactivity > timeoutThreshold) {
            handleBothPlayersTimeout(match, player1Inactivity, player2Inactivity)
        } else if (player1Inactivity > timeoutThreshold) {
            assignTechnicalDefeat(match, match.player1)
        } else if (player2Inactivity > timeoutThreshold) {
            assignTechnicalDefeat(match, match.player2)
        }
    }

    private fun getLastResultOrStartTime(match: Match, player: User): LocalDateTime {
        val lastResult = roomResultRepository.findTopByMatchAndPlayerOrderByCompletedAtDesc(match, player)
        return lastResult?.completedAt ?: match.startedAt
    }

    private fun handleBothPlayersTimeout(match: Match, player1Inactivity: Duration, player2Inactivity: Duration) {
        if (player1Inactivity < player2Inactivity) {
            assignTechnicalDefeat(match, match.player2)
        } else if (player2Inactivity < player1Inactivity) {
            assignTechnicalDefeat(match, match.player1)
        } else {
            match.status = MatchStatus.DRAW
            match.endedAt = LocalDateTime.now()
            match.winner = null
            matchRepository.save(match)

            logger.info("Match ${match.id} ended in draw due to both players timeout")
        }
    }

    private fun assignTechnicalDefeat(match: Match, timeoutPlayer: User) {
        val winner = if (timeoutPlayer == match.player1) match.player2 else match.player1

        match.status = MatchStatus.COMPLETED
        match.endedAt = LocalDateTime.now()
        match.winner = winner

        matchRepository.save(match)

        logger.info("Technical defeat assigned to player ${timeoutPlayer.id} in match ${match.id}")
    }

    private fun validateRetryLimits(user: User, match: Match) {
        val retries = match.roomRetries.filter { it.player == user }

        if (retries.size >= MAX_RETRIES_COUNT) {
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

    private fun createRoomResult(user: User, match: Match, request: SubmitRoomResultRequest, penalty: Int): RoomResult = RoomResult(
        roomNumber = request.roomNumber,
        time = request.time + penalty,
    ).apply {
        this.player = user
        this.match = match
    }

    private fun saveRoomResult(roomResult: RoomResult): RoomResult {
        try {
            return roomResultRepository.save(roomResult)
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already submitted result to this room",
            )
        }
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
}

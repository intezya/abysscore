package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.RoomResult
import com.intezya.abysscore.model.entity.RoomRetry
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.MatchRepository
import com.intezya.abysscore.repository.RoomResultRepository
import com.intezya.abysscore.repository.RoomRetryRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

private const val MAX_RETRIES_COUNT = 5
private const val RETRY_PENALTY = 5

@Service
@Transactional
class MatchProcessService(
    private val roomResultRepository: RoomResultRepository,
    private val roomRetryRepository: RoomRetryRepository,
    private val matchRepository: MatchRepository,
) {
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

    private fun validateRetryLimits(user: User, match: Match) {
        val retries = match.roomRetries.filter { it.user == user }

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
        this.user = user
        this.match = match
    }

    private fun saveAndAddRetry(match: Match, roomRetry: RoomRetry) {
        val savedRetry = roomRetryRepository.save(roomRetry)
        match.roomRetries.add(savedRetry)
    }

    private fun calculatePenalty(user: User, match: Match, roomNumber: Int): Int {
        val usedRetries = roomRetryRepository.countByUserAndMatchAndRoomNumber(
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
        this.user = user
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

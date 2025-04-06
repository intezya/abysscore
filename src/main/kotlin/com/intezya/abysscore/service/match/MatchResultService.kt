package com.intezya.abysscore.service.match

import com.intezya.abysscore.event.match.process.MatchSubmitResultEvent
import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.match.MatchRoomResult
import com.intezya.abysscore.model.entity.match.MatchRoomRetry
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchRepository
import com.intezya.abysscore.repository.RoomResultRepository
import com.intezya.abysscore.repository.RoomRetryRepository
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

private const val MAX_RETRIES_COUNT = 5
private const val RETRY_PENALTY = 5

@Service
@Transactional
class MatchResultService(
    private val roomResultRepository: RoomResultRepository,
    private val roomRetryRepository: RoomRetryRepository,
    private val matchRepository: MatchRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun processRetry(user: User, match: Match, request: SubmitRoomResultRequest): Match {
        validateRetryLimits(user, match)

        val roomRetry = createRoomRetry(user, match, request)
        saveAndAddRetry(match, roomRetry)
        matchRepository.save(match)
        return match
    }

    fun processResult(user: User, match: Match, request: SubmitRoomResultRequest): Match {
        val penalty = calculatePenalty(user, match, request.roomNumber)
        val roomResult = createRoomResult(user, match, request, penalty)

        try {
            val savedResult = roomResultRepository.save(roomResult)
            match.roomResults.add(savedResult)

            eventPublisher.publishEvent(MatchSubmitResultEvent(this, match, savedResult))
            return matchRepository.save(match)
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already submitted result to this room",
            )
        }
    }

    fun saveMatch(match: Match): Match = matchRepository.save(match)

    private fun validateRetryLimits(user: User, match: Match) {
        val retries = match.roomRetries.count { it.player == user }
        if (retries >= MAX_RETRIES_COUNT) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already submitted too many retries",
            )
        }
    }

    private fun createRoomRetry(user: User, match: Match, request: SubmitRoomResultRequest): MatchRoomRetry =
        MatchRoomRetry(
            roomNumber = request.roomNumber,
            time = request.time,
        ).apply {
            this.player = user
            this.match = match
        }

    private fun saveAndAddRetry(match: Match, matchRoomRetry: MatchRoomRetry) {
        val savedRetry = roomRetryRepository.save(matchRoomRetry)
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

    private fun createRoomResult(
        user: User,
        match: Match,
        request: SubmitRoomResultRequest,
        penalty: Int,
    ): MatchRoomResult = MatchRoomResult(
        roomNumber = request.roomNumber,
        time = request.time + penalty,
    ).apply {
        this.player = user
        this.match = match
    }

    private fun processPenaltyTime(retriesCount: Long): Int = when (retriesCount) {
        in 4L..5L -> RETRY_PENALTY
        else -> 0
    }
}

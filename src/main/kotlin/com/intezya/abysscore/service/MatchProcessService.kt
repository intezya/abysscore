package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.RoomResult
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.RoomResultRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class MatchProcessService(
    private val roomResultRepository: RoomResultRepository,
) {
    fun submitResult(user: User, roomResultRequest: SubmitRoomResultRequest): Match {
        val currentMatch = user.currentMatch!!

        val userResults = roomResultRepository.findByMatchAndUserAndRoomNumberOrderByCompletedAtDesc(
            currentMatch,
            user,
            roomResultRequest.roomNumber,
        )

        if (userResults.size == currentMatch.maxRetries) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You have reached the maximum number of attempts for this room",
            )
        }

        val roomResult = RoomResult(
            roomNumber = roomResultRequest.roomNumber,
            time = roomResultRequest.time,
        ).apply {
            this.user = user
            this.match = currentMatch
        }

        val savedRoomResult = roomResultRepository.save(roomResult)
        currentMatch.roomResults.add(savedRoomResult)

        // TODO: check if room results is full

        // TODO: notify opponent

        return currentMatch
    }
}

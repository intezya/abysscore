package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DraftValidationService {
    fun validateMatchStatus(user: User, expectedStatus: MatchStatus, errorMessage: String): Match {
        val match = user.currentMatch ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in a match")

        if (match.status != expectedStatus) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        return match
    }

    fun validateDraftState(draft: MatchDraft, expectedState: DraftState) {
        if (draft.currentState != expectedState) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Not in needed phase. Current state: ${draft.currentState}",
            )
        }
    }

    fun validateUserTurn(draft: MatchDraft, isPlayer1: Boolean) {
        val isUserTurn = (isPlayer1 && draft.isCurrentTurnPlayer1()) || (!isPlayer1 && draft.isCurrentTurnPlayer2())
        if (!isUserTurn) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Not your turn to make a move",
            )
        }
    }

    fun getPlayerInfo(match: Match, userId: Long): PlayerInfo {
        val isPlayer1 = match.player1.id == userId
        val isPlayer2 = match.player2.id == userId

        if (!isPlayer1 && !isPlayer2) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User with ID $userId is not part of this match",
            )
        }

        return PlayerInfo(
            player = if (isPlayer1) match.player1 else match.player2,
            isPlayer1 = isPlayer1,
        )
    }
}

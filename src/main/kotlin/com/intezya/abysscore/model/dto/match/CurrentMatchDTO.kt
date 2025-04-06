package com.intezya.abysscore.model.dto.match

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.draft.MatchDraftDTO
import com.intezya.abysscore.model.dto.draft.toDTO
import com.intezya.abysscore.model.dto.roomresult.RoomResultDTO
import com.intezya.abysscore.model.dto.roomresult.RoomRetryDTO
import com.intezya.abysscore.model.dto.roomresult.toDTO
import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.match.Match
import java.time.LocalDateTime

data class CurrentMatchDTO(
    val id: Long,
    val createdAt: LocalDateTime,
    val startedAt: LocalDateTime,
    val status: MatchStatus,
    val player1: UserSimpleViewDTO,
    val player2: UserSimpleViewDTO,
    val roomResults: List<RoomResultDTO>,
    val roomRetries: List<RoomRetryDTO>,
    val draft: MatchDraftDTO,
) {
    constructor(match: Match) : this(
        id = match.id,
        createdAt = match.createdAt,
        startedAt = match.startedAt,
        status = match.status,
        player1 = match.player1.toSimpleView(),
        player2 = match.player2.toSimpleView(),
        roomResults = match.roomResults.map { it.toDTO() },
        roomRetries = match.roomRetries.map { it.toDTO() },
        draft = match.draft.toDTO(),
    )
}

fun Match.toCurrentMatchDTO(): CurrentMatchDTO = CurrentMatchDTO(this)

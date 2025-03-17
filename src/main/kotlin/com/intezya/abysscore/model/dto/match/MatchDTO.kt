package com.intezya.abysscore.model.dto.match

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.roomresult.RoomResultDTO
import com.intezya.abysscore.model.dto.roomresult.RoomRetryDTO
import com.intezya.abysscore.model.dto.roomresult.toDTO
import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.Match
import java.time.LocalDateTime

data class MatchDTO(
    val id: Long,
    val createdAt: LocalDateTime,
    var startedAt: LocalDateTime,
    var endedAt: LocalDateTime?,
    var status: MatchStatus,
    var winner: UserSimpleViewDTO?,
    val player1: UserSimpleViewDTO,
    val player2: UserSimpleViewDTO,
    val roomResults: List<RoomResultDTO>,
    val roomRetries: List<RoomRetryDTO>,
) {
    constructor(match: Match) : this(
        id = match.id,
        createdAt = match.createdAt,
        startedAt = match.startedAt,
        endedAt = match.endedAt,
        status = match.status,
        winner = match.winner?.toSimpleView(),
        player1 = match.player1.toSimpleView(),
        player2 = match.player2.toSimpleView(),
        roomResults = match.roomResults.map { it.toDTO() },
        roomRetries = match.roomRetries.map { it.toDTO() },
    )
}

fun Match.toDTO(): MatchDTO = MatchDTO(this)

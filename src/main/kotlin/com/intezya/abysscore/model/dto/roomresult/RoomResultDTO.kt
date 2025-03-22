package com.intezya.abysscore.model.dto.roomresult

import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.MatchRoomResult
import java.time.LocalDateTime

data class RoomResultDTO(
    val user: UserSimpleViewDTO,
    val roomNumber: Int,
    val time: Int,
    val completedAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor(matchRoomResult: MatchRoomResult) : this(
        user = matchRoomResult.player.toSimpleView(),
        roomNumber = matchRoomResult.roomNumber,
        time = matchRoomResult.time,
        completedAt = matchRoomResult.completedAt,
    )
}

fun MatchRoomResult.toDTO(): RoomResultDTO = RoomResultDTO(this)

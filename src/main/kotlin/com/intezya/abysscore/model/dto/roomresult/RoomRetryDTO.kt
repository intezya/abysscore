package com.intezya.abysscore.model.dto.roomresult

import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.MatchRoomRetry
import java.time.LocalDateTime

data class RoomRetryDTO(
    val user: UserSimpleViewDTO,
    val roomNumber: Int,
    val time: Int,
    val completedAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor(roomResult: MatchRoomRetry) : this(
        user = roomResult.player.toSimpleView(),
        roomNumber = roomResult.roomNumber,
        time = roomResult.time,
        completedAt = roomResult.completedAt,
    )
}

fun MatchRoomRetry.toDTO(): RoomRetryDTO = RoomRetryDTO(this)

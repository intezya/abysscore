package com.intezya.abysscore.model.dto.roomresult

import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.RoomResult
import java.time.LocalDateTime

data class RoomResultDTO(
    val id: Long,
    val user: UserSimpleViewDTO,
    val roomNumber: Int,
    val time: Long,
    val completedAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor(roomResult: RoomResult) : this(
        id = roomResult.id,
        user = roomResult.user.toSimpleView(),
        roomNumber = roomResult.roomNumber,
        time = roomResult.time,
        completedAt = roomResult.completedAt,
    )
}

fun RoomResult.toDTO(): RoomResultDTO = RoomResultDTO(this)

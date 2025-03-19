package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.RoomRetry
import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRetryRepository : JpaRepository<RoomRetry, Long> {
    fun countByPlayerAndMatchAndRoomNumber(player: User, match: Match, roomNumber: Int): Long
}

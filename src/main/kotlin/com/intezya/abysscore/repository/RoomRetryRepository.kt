package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.match.MatchRoomRetry
import com.intezya.abysscore.model.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRetryRepository : JpaRepository<MatchRoomRetry, Long> {
    fun countByPlayerAndMatchAndRoomNumber(player: User, match: Match, roomNumber: Int): Long
}

package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.MatchRoomResult
import com.intezya.abysscore.model.entity.MatchRoomRetry
import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoomResultRepository : JpaRepository<MatchRoomResult, Long> {
    fun findTopByMatchAndPlayerOrderByCompletedAtDesc(match: Match, player: User): MatchRoomRetry?
}

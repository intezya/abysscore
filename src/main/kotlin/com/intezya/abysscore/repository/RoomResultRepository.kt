package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.match.MatchRoomResult
import com.intezya.abysscore.model.entity.match.MatchRoomRetry
import com.intezya.abysscore.model.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoomResultRepository : JpaRepository<MatchRoomResult, Long> {
    fun findTopByMatchAndPlayerOrderByCompletedAtDesc(match: Match, player: User): MatchRoomRetry?
}

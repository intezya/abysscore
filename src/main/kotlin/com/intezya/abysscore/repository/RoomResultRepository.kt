package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.RoomResult
import com.intezya.abysscore.model.entity.RoomRetry
import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface RoomResultRepository : JpaRepository<RoomResult, Long> {
    fun findTopByMatchAndPlayerOrderByCompletedAtDesc(match: Match, player: User): RoomRetry?
}

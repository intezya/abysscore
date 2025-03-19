package com.intezya.abysscore.repository

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.entity.Match
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : JpaRepository<Match, Long> {
    fun findByStatus(status: MatchStatus): List<Match>
}

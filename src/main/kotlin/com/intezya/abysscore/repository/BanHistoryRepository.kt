package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.user.BanHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

private const val FIND_ACTIVE_BANS_QUERY = """
    SELECT b
    FROM BanHistory b
    WHERE
        b.user.id = :userId AND
        (b.expiresAt IS NULL OR b.expiresAt > :now) AND
        b.disputeApproved = false
"""

interface BanHistoryRepository : JpaRepository<BanHistory, Long> {
    @Query(FIND_ACTIVE_BANS_QUERY)
    fun findActiveBansForUser(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now(),
    ): List<BanHistory>
}

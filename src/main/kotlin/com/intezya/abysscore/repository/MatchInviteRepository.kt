package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.MatchInvite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

private const val FIND_ACTIVE_BY_INVITER_ID_AND_INVITEE_ID_QUERY = """
    SELECT m FROM MatchInvite m
    WHERE 
        m.inviterId = :inviterId AND
        m.inviteeId = :inviteeId AND
        m.createdAt > CURRENT_TIMESTAMP - INTERVAL m.activeDiffSeconds SECOND
"""

private const val FIND_ACTIVE_BY_INVITER_ID_QUERY = """
    SELECT m FROM MatchInvite m
    WHERE 
        m.inviterId = :inviterId AND
        m.createdAt > CURRENT_TIMESTAMP - INTERVAL m.activeDiffSeconds SECOND
"""

interface MatchInviteRepository : JpaRepository<MatchInvite, Long> {
    @Query(
        FIND_ACTIVE_BY_INVITER_ID_AND_INVITEE_ID_QUERY,
        nativeQuery = true,
    )
    fun findActiveByInviterIdAndInviteeId(
        inviterId: Long,
        inviteeId: Long,
    ): Optional<MatchInvite>

    @Query(
        FIND_ACTIVE_BY_INVITER_ID_QUERY,
        nativeQuery = true,
    )
    fun findActiveByInviterId(
        inviterId: Long,
        pageable: Pageable,
    ): Page<MatchInvite>

    fun findByIdAndInviteeId(id: Long, inviteeId: Long): Optional<MatchInvite>
}

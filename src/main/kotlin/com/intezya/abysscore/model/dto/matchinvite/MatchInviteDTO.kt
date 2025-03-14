package com.intezya.abysscore.model.dto.matchinvite

import com.intezya.abysscore.model.entity.MatchInvite
import com.intezya.abysscore.model.entity.User
import java.time.LocalDateTime

data class MatchInviteDTO(
    var id: Long,
    val inviterId: User,
    val inviteeId: User,
    val expiresAt: LocalDateTime,
) {
    constructor(matchInvite: MatchInvite) : this(
        id = matchInvite.id,
        inviterId = matchInvite.inviter!!,
        inviteeId = matchInvite.invitee!!,
        expiresAt = matchInvite.createdAt.plusSeconds(matchInvite.activeDiffSeconds),
    )
}

fun MatchInvite.toDTO() = MatchInviteDTO(this)

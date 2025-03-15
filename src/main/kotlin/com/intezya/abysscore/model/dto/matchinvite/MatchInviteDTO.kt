package com.intezya.abysscore.model.dto.matchinvite

import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.simpleView
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.model.entity.MatchInvite
import java.time.LocalDateTime

data class MatchInviteDTO(
    var id: Long,
    val inviter: UserDTO,
    val invitee: UserSimpleViewDTO,
    val expiresAt: LocalDateTime,
) {
    constructor(matchInvite: MatchInvite) : this(
        id = matchInvite.id,
        inviter = matchInvite.inviter!!.toDTO(),
        invitee = matchInvite.invitee!!.toDTO().simpleView(),
        expiresAt = matchInvite.createdAt.plusSeconds(matchInvite.activeDiffSeconds),
    )
}

fun MatchInvite.toDTO() = MatchInviteDTO(this)

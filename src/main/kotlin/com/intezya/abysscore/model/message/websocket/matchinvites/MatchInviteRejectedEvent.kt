package com.intezya.abysscore.model.message.websocket.matchinvites

import com.intezya.abysscore.model.message.websocket.Messages

// TODO: change rejected to declined
data class MatchInviteRejectedEvent(
    val inviteId: Long,
    val inviteeUsername: String,
    val message: String = Messages.MATCH_INVITE_DECLINED,
)

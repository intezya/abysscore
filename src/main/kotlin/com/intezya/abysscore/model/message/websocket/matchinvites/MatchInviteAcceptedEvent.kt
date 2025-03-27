package com.intezya.abysscore.model.message.websocket.matchinvites

import com.intezya.abysscore.model.message.websocket.Messages

data class MatchInviteAcceptedEvent(
    val inviteId: Long,
    val inviteeUsername: String,
    val message: String = Messages.MATCH_INVITE_ACCEPTED,
)

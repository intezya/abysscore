package com.intezya.abysscore.model.message.websocket.matchinvites

data class MatchInviteAcceptedEvent(
    val inviteId: Long,
    val inviteeUsername: String,
)

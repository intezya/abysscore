package com.intezya.abysscore.model.message.websocket.matchinvites

data class MatchInviteReceivedEvent(
    val inviteId: Long,
    val inviterUsername: String,
)

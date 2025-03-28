package com.intezya.abysscore.model.message.websocket.matchinvites

import com.intezya.abysscore.model.message.websocket.Messages

data class MatchInviteReceivedEvent(
    val inviteId: Long,
    val inviterUsername: String,
    val message: String = Messages.MATCH_INVITE_RECEIVED,
)

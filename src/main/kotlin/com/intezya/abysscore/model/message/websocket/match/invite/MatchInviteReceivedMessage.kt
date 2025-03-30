package com.intezya.abysscore.model.message.websocket.match.invite

import com.intezya.abysscore.model.message.websocket.Messages

data class MatchInviteReceivedMessage(
    val inviteId: Long,
    val inviterUsername: String,
    val message: String = Messages.MATCH_INVITE_RECEIVED,
)

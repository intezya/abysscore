package com.intezya.abysscore.model.message.websocket.match.invite

import com.intezya.abysscore.model.message.websocket.Messages

data class MatchInviteAcceptedMessage(
    val inviteId: Long,
    val inviteeUsername: String,
    val message: String = Messages.MATCH_INVITE_ACCEPTED,
) : BaseMatchInviteWebsocketMessage()

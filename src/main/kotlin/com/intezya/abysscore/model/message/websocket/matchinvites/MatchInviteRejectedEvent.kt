package com.intezya.abysscore.model.message.websocket.matchinvites

data class MatchInviteRejectedEvent(val inviteId: Long, val inviteeUsername: String)

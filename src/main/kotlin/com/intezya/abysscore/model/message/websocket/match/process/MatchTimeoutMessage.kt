package com.intezya.abysscore.model.message.websocket.match.process

import com.intezya.abysscore.model.message.websocket.matchmaking.BaseMatchMakingMessage

data class MatchTimeoutMessage(
    val matchId: Long,
    val result: String, // "victory", "defeat", "draw"
    val reason: String,
) : BaseMatchMakingMessage()

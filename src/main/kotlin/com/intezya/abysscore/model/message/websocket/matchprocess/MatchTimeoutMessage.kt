package com.intezya.abysscore.model.message.websocket.matchprocess

data class MatchTimeoutMessage(
    val matchId: Long,
    val result: String,  // "victory", "defeat", "draw"
    val reason: String,
)

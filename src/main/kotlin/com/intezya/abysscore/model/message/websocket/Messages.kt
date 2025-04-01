package com.intezya.abysscore.model.message.websocket

object Messages {
    const val USER_DISCONNECTED = "User disconnected"
    const val MATCH_INVITE_ACCEPTED = "Match invite accepted"
    const val MATCH_INVITE_DECLINED = "Match invite declined"
    const val MATCH_INVITE_RECEIVED = "Match invite received"
    const val MATCH_CREATED = "Match created"
    const val DRAFT_CHARACTERS_REVEAL = "Opponent revealed characters"
    const val DRAFT_ACTION_PERFORMED = "Opponent performed draft action"
    const val DRAFT_AUTOMATIC_ACTION_PERFORMED = "Opponent performed automatic draft action caused by timeout"
}

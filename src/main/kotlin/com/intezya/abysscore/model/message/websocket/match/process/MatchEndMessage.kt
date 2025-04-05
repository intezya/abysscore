package com.intezya.abysscore.model.message.websocket.match.process

import com.intezya.abysscore.enum.UserMatchResult

data class MatchEndMessage(val score: Int, val opponentScore: Int, val winner: UserMatchResult) :
    BaseMatchProcessMessage()

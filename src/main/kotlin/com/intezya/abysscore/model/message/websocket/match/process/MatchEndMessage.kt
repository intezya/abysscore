package com.intezya.abysscore.model.message.websocket.match.process

import com.intezya.abysscore.enum.UserMatchResult
import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class MatchEndMessage(
    val score: Int,
    val opponentScore: Int,
    val myResult: UserMatchResult,
    val winner: UserSimpleViewDTO?,
) : BaseMatchProcessMessage() {
    val message = Messages.MATCH_END
}

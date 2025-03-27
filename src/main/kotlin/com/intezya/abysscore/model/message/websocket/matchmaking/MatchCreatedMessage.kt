package com.intezya.abysscore.model.message.websocket.matchmaking

import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class MatchCreatedMessage(val matchId: Long, val opponent: UserDTO, val message: String = Messages.MATCH_CREATED)

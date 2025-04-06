package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.message.websocket.Messages

data class DraftEndMessage(val player1Characters: List<String>, val player2Characters: List<String>) :
    BaseDraftProcessMessage() {
    val message: String = Messages.DRAFT_END
}

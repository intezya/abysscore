package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class DraftEndMessage(
    val player1Characters: List<DraftCharacterDTO>,
    val player2Characters: List<DraftCharacterDTO>,
) : BaseDraftProcessMessage() {
    val message: String = Messages.DRAFT_END
}

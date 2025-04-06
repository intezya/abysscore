package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.dto.draft.MatchDraftDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class BothPlayersReadyForDraftMessage(val matchDraft: MatchDraftDTO) : BaseDraftProcessMessage() {
    val message = Messages.DRAFT_BOTH_PLAYERS_READY
}

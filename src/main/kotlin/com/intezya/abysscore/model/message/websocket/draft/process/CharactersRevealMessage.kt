package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class CharactersRevealMessage(val characters: List<DraftCharacterDTO>) : BaseDraftProcessMessage() {
    val message: String = Messages.DRAFT_CHARACTERS_REVEAL
}

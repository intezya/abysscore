package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class CharactersRevealMessage(
    val message: String = Messages.DRAFT_CHARACTERS_REVEAL,
    val characters: List<DraftCharacterDTO>,
)

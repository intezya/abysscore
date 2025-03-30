package com.intezya.abysscore.event.draftprocess

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User
import org.springframework.context.ApplicationEvent

class CharactersRevealEvent(source: Any, val match: Match, val player: User, val characters: List<DraftCharacterDTO>) :
    ApplicationEvent(source)

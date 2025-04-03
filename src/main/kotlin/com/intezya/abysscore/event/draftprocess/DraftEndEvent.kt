package com.intezya.abysscore.event.draftprocess

import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.match.Match
import org.springframework.context.ApplicationEvent

class DraftEndEvent(source: Any, val match: Match, val draft: MatchDraft) : ApplicationEvent(source)

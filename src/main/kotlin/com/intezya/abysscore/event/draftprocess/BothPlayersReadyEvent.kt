package com.intezya.abysscore.event.draftprocess

import com.intezya.abysscore.model.entity.match.Match
import org.springframework.context.ApplicationEvent

class BothPlayersReadyEvent(source: Any, val match: Match) : ApplicationEvent(source)

package com.intezya.abysscore.event.matchmaking

import com.intezya.abysscore.model.entity.match.Match
import org.springframework.context.ApplicationEvent

class MatchCreatedEvent(source: Any, val match: Match) : ApplicationEvent(source)

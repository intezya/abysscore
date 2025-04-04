package com.intezya.abysscore.event.match.process

import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.match.MatchRoomResult
import org.springframework.context.ApplicationEvent

class MatchSubmitResultEvent(source: Any, val match: Match, val result: MatchRoomResult) : ApplicationEvent(source)

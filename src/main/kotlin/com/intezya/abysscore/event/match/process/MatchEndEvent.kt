package com.intezya.abysscore.event.match.process

import com.intezya.abysscore.model.entity.match.Match
import org.springframework.context.ApplicationEvent

class MatchEndEvent(source: Any, val match: Match, val player1Score: Int, val player2Score: Int) :
    ApplicationEvent(source)

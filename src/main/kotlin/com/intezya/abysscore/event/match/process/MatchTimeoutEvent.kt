package com.intezya.abysscore.event.match.process

import com.intezya.abysscore.enum.TimeoutResult
import com.intezya.abysscore.model.entity.match.Match
import org.springframework.context.ApplicationEvent
import java.time.Duration

class MatchTimeoutEvent(
    source: Any,
    val match: Match,
    val timeoutResult: TimeoutResult,
    val player1Inactivity: Duration,
    val player2Inactivity: Duration,
) : ApplicationEvent(source)

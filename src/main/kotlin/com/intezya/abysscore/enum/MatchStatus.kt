package com.intezya.abysscore.enum

import java.time.Duration

enum class MatchStatus(val timeout: Duration) {
    PENDING(Duration.ofMinutes(5)),
    DRAFTING(Duration.ofMinutes(2)),
    ACTIVE(Duration.ofMinutes(15)),
    COMPLETED(Duration.ZERO),
    CANCELED(Duration.ZERO),
    DRAW(Duration.ZERO),
}

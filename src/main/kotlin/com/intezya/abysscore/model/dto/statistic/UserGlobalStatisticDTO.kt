package com.intezya.abysscore.model.dto.statistic

import com.intezya.abysscore.model.entity.UserGlobalStatistic

data class UserGlobalStatisticDTO(
    var matchesWon: Int = 0,
    var matchesLost: Int = 0,
    var matchesDraws: Int = 0,
    var summaryTimeClear: Int = 0,
    val xp: Int = 0,
) {
    constructor(userGlobalStatistic: UserGlobalStatistic) : this(
        matchesWon = userGlobalStatistic.matchesWon,
        matchesLost = userGlobalStatistic.matchesLost,
        matchesDraws = userGlobalStatistic.matchesDraws,
        summaryTimeClear = userGlobalStatistic.summaryTimeClear,
        xp = userGlobalStatistic.xp,
    )
}

fun UserGlobalStatistic.toDTO(): UserGlobalStatisticDTO = UserGlobalStatisticDTO(this)

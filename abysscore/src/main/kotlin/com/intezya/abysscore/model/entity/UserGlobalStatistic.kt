package com.intezya.abysscore.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_global_statistics")
data class UserGlobalStatistic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User? = null,

    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val summaryTimeClear: Int = 0,
    val xp: Int = 0,

) {
    constructor() : this(
        id = 0L,
        user = null,
        wins = 0,
        losses = 0,
        draws = 0,
        summaryTimeClear = 0,
        xp = 0,
    )
}

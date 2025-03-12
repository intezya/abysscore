package com.intezya.abysscore.model.entity

import jakarta.persistence.*


@Entity
@Table(name = "user_global_statistics")
data class UserGlobalStatistic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User? = null,

    val wins : Int = 0,
    val losses : Int = 0,
    val draws : Int = 0,
    val summaryTimeClear : Int = 0,
    val xp : Int = 0,

) {
    constructor() : this(null, null, 0, 0, 0, 0, 0)
}

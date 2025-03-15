package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_global_statistics")
data class UserGlobalStatistic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,

    @Column(nullable = false)
    var matchesWon: Int = 0,

    @Column(nullable = false)
    var matchesLost: Int = 0,

    @Column(nullable = false)
    var matchesDraws: Int = 0,

    @Column(nullable = false)
    var summaryTimeClear: Int = 0,

    @Column(nullable = false)
    val xp: Int = 0,

    @Column(nullable = false)
    var skill: Int = 1000,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

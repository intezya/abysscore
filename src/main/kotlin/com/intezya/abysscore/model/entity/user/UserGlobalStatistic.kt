package com.intezya.abysscore.model.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_global_statistics")
class UserGlobalStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(nullable = false)
    var matchesWon: Int = 0

    @Column(nullable = false)
    var matchesLost: Int = 0

    @Column(nullable = false)
    var matchesDraws: Int = 0

    @Column(nullable = false)
    var summaryTimeClear: Int = 0

    @Column(nullable = false)
    val xp: Int = 0

    @Column(nullable = false)
    var skill: Int = 1000

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    lateinit var user: User

    constructor()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(matchesWon, matchesLost, matchesDraws, summaryTimeClear, xp, skill, createdAt, updatedAt, user)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserGlobalStatistic) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            matchesWon == other.matchesWon &&
                matchesLost == other.matchesLost &&
                matchesDraws == other.matchesDraws &&
                summaryTimeClear == other.summaryTimeClear &&
                xp == other.xp &&
                skill == other.skill &&
                createdAt == other.createdAt &&
                updatedAt == other.updatedAt &&
                user == other.user
        }
    }

    override fun toString(): String = this::class.simpleName +
        "(id = $id , matchesWon = $matchesWon , matchesLost = $matchesLost , matchesDraws = $matchesDraws , summaryTimeClear = $summaryTimeClear , xp = $xp , skill = $skill , createdAt = $createdAt , updatedAt = $updatedAt )"
}

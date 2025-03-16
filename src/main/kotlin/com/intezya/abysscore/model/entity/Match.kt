package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.MatchStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "matches")
data class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tournament_id")
//    val tournament: Tournament? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var endedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MatchStatus = MatchStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    var winner: User? = null,

    @Column(nullable = false)
    var player1Score: Int = 0,

    @Column(nullable = false)
    var player2Score: Int = 0,

    @Column(nullable = true)
    val matchmakingData: String? = null,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    lateinit var player1: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    lateinit var player2: User

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Match) return false
        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            createdAt == other.createdAt &&
                startedAt == other.startedAt &&
                endedAt == other.endedAt &&
                status == other.status &&
                winner == other.winner &&
                player1Score == other.player1Score &&
                player2Score == other.player2Score &&
                player1 == other.player1 &&
                player2 == other.player2 &&
                matchmakingData == other.matchmakingData
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(
            createdAt, startedAt, endedAt, status, winner,
            player1Score, player2Score, player1, player2, matchmakingData,
        )
    }

    @Override
    override fun toString(): String = this::class.simpleName + "(id = $id , createdAt = $createdAt , startedAt = $startedAt , endedAt = $endedAt , status = $status , player1Score = $player1Score , player2Score = $player2Score , matchmakingData = $matchmakingData )"
}

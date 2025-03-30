package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.MatchStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "matches")
class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tournament_id")
//    val tournament: Tournament? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false, updatable = false)
    var startedAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = true)
    var endedAt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MatchStatus = MatchStatus.PENDING

    @Column(nullable = true)
    var technicalDefeatReason: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    var winner: User? = null

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val roomResults: MutableList<MatchRoomResult> = mutableListOf()

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val roomRetries: MutableList<MatchRoomRetry> = mutableListOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    lateinit var player1: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    lateinit var player2: User

    @OneToOne(mappedBy = "match", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var draft: MatchDraft

    constructor()

    constructor(
        player1: User,
        player2: User,
    ) {
        this.player1 = player1
        this.player2 = player2
    }

    fun isEnded(): Boolean = this.roomResults.filter { it.roomNumber == 3 }.size == 2

    fun getOpponent(user: User): User = if (user.id == player1.id) player2 else player1

    fun hasPlayerAlreadyRevealedCharacters(user: User): Boolean {
        return (this.player1.id == user.id && this.draft.player1AvailableCharacters.isNotEmpty()) ||
            (this.player2.id == user.id && this.draft.player2AvailableCharacters.isNotEmpty())
    }

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
                player1 == other.player1 &&
                player2 == other.player2
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(
            createdAt,
            startedAt,
            endedAt,
            status,
            winner,
            player1,
            player2,
        )
    }

    override fun toString(): String = this::class.simpleName +
        "(id = $id , createdAt = $createdAt , startedAt = $startedAt , endedAt = $endedAt , status = $status )"
}

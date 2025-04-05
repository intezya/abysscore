package com.intezya.abysscore.model.entity.match

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.enum.UserMatchResult
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

const val MATCH_MAX_ROOM_RESULTS_COUNT = 6

@Entity
@Table(name = "matches")
class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

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

    fun isRoomResultsFilled(): Boolean = roomResults.size == MATCH_MAX_ROOM_RESULTS_COUNT &&
        roomResults.count { it.roomNumber == 3 } == 2

    fun getOpponent(user: User): User = when (user.id) {
        player1.id -> player2
        player2.id -> player1
        else -> throw IllegalArgumentException("User is not a participant of this match")
    }

    fun hasPlayerAlreadyRevealedCharacters(user: User): Boolean = when (user.id) {
        player1.id -> draft.player1AvailableCharacters.isNotEmpty()
        player2.id -> draft.player2AvailableCharacters.isNotEmpty()
        else -> false
    }

    fun getResultForPlayer(player: User): Boolean? {
        if (status != MatchStatus.COMPLETED) return null
        return winner?.id == player.id
    }

    fun isPlayerParticipant(user: User): Boolean = user.id == player1.id || user.id == player2.id

    fun getPlayerScore(player: User) = roomResults.filter { it.player == player }.sumOf { it.time }

    fun determineResultForPlayer(player: User) = when (winner) {
        null -> UserMatchResult.DRAW
        player -> UserMatchResult.WINNER
        else -> UserMatchResult.LOSER
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Match) return false

        if (id != 0L && other.id != 0L) {
            return id == other.id
        }

        return createdAt == other.createdAt &&
            startedAt == other.startedAt &&
            status == other.status &&
            player1.id == other.player1.id &&
            player2.id == other.player2.id
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(
            createdAt,
            startedAt,
            status,
            player1.id,
            player2.id,
        )
    }

    override fun toString(): String =
        "Match(id=$id, status=$status, player1=${player1.id}, player2=${player2.id}, started=$startedAt, ended=$endedAt)"
}

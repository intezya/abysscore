package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "room_results",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_room_player_match_no_retry",
            // You must check that migration index contains room_number
            columnNames = ["room_number", "player_id", "match_id"],
        ),
    ],
)
data class MatchRoomResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val roomNumber: Int,

    @Column(nullable = false)
    val time: Int,

    @Column(nullable = false)
    val completedAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(
        id = 0L,
        roomNumber = 0,
        time = 0,
    )

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    lateinit var player: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    lateinit var match: Match
}

package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "room_results")
data class RoomResult(
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
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    lateinit var match: Match
}

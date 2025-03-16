package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "room_results")
data class RoomResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: Match,

    @Column(nullable = false)
    val roomNumber: Int,

    @Column(nullable = false)
    val time: Long,

    @Column(nullable = false)
    val completedAt: LocalDateTime = LocalDateTime.now(),
)

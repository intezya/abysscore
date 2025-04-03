package com.intezya.abysscore.model.entity.match

import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "room_results",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_room_player_match_no_retry",
            columnNames = ["match_id", "player_id", "room_number"],
        ),
    ],
)
class MatchRoomResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = false)
    val roomNumber: Int

    @Column(nullable = false)
    val time: Int

    @Column(nullable = false)
    val completedAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    lateinit var player: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    lateinit var match: Match

    constructor() : this(0, 0)

    constructor(
        roomNumber: Int,
        time: Int,
    ) {
        this.roomNumber = roomNumber
        this.time = time
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchRoomResult) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "MatchRoomResult(id=$id, roomNumber=$roomNumber)"
}

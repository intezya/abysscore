package com.intezya.abysscore.model.entity.match.matchmaking

import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "match_invites")
class MatchInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(name = "active_diff_seconds", nullable = false)
    var activeDiffSeconds: Long = 0L

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    lateinit var inviter: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    lateinit var invitee: User

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    constructor()

    constructor(activeDiffSeconds: Long) {
        this.activeDiffSeconds = activeDiffSeconds
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchInvite) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "MatchInvite(id=$id)"
}

package com.intezya.abysscore.model.entity.match.matchmaking

import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "match_invites")
class MatchInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = 0L

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "active_diff_seconds", nullable = false)
    var activeDiffSeconds: Long = 0L

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    lateinit var inviter: User

    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    lateinit var invitee: User

    constructor()

    constructor(
        id: Long = 0L,
        activeDiffSeconds: Long = 0L,
    ) {
        this.id = id
        this.activeDiffSeconds = activeDiffSeconds
    }

    constructor(
        inviter: User,
        invitee: User,
        activeDiffSeconds: Long = 0L,
    ) {
        this.inviter = inviter
        this.invitee = invitee
        this.activeDiffSeconds = activeDiffSeconds
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchInvite) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            inviter == other.inviter &&
                invitee == other.invitee &&
                createdAt == other.createdAt
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(inviter, invitee, createdAt)
    }

    override fun toString(): String = this::class.simpleName +
        "(id = $id , inviter = $inviter , invitee = $invitee , createdAt = $createdAt , activeDiffSeconds = $activeDiffSeconds )"
}

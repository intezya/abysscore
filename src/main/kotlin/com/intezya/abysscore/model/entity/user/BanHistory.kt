package com.intezya.abysscore.model.entity.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ban_history")
class BanHistory(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, updatable = false)
    val expiresAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banned_by", nullable = true, updatable = false)
    val bannedBy: User? = null,

    @Column(nullable = true, updatable = false)
    val reason: String? = null,
) {
    constructor() : this(User(), LocalDateTime.now())

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column
    var disputeApproved: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BanHistory) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            user.id == other.user.id && createdAt == other.createdAt && other.expiresAt == expiresAt
        }
    }

    override fun hashCode() = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(user.id, createdAt, expiresAt)
    }

    override fun toString(): String = buildString {
        append(this@BanHistory::class.simpleName)
        append("(id=$id)")
        append("(user_id=${user.id}, username=${user.username})")
        append("createdAt=$createdAt, expiresAt=$expiresAt, ")
        append("disputeApproved=$disputeApproved)")
    }
}

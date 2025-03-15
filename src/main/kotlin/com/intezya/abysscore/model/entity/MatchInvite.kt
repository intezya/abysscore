package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "match_invites",
)
data class MatchInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    val inviter: User? = null,

    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    val invitee: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "active_diff_seconds", nullable = false)
    var activeDiffSeconds: Long = 0L,
)

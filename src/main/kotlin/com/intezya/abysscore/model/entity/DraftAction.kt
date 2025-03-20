package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.DraftActionType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "draft_actions")
data class DraftAction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    val draft: MatchDraft,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val actionType: DraftActionType,

    @Column(nullable = true)
    val characterName: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val isTimeoutAction: Boolean = false,
)

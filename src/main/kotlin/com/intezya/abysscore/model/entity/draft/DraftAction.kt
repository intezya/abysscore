package com.intezya.abysscore.model.entity.draft

import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "draft_actions")
class DraftAction(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    val draft: MatchDraft,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    val player: User,

    @Column(nullable = false, updatable = false)
    val characterName: String,

    @Column(nullable = false, updatable = false)
    val isPick: Boolean,

    @Column(nullable = false, updatable = false)
    val stepIndex: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    constructor() : this(
        draft = MatchDraft(),
        player = User(),
        characterName = "",
        isPick = false,
        stepIndex = 0,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DraftAction) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            draft == other.draft &&
                player == other.player &&
                isPick == other.isPick &&
                characterName == other.characterName &&
                stepIndex == other.stepIndex &&
                createdAt == other.createdAt
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(draft, player, isPick, characterName, stepIndex, createdAt)
    }

    override fun toString(): String =
        "DraftAction(id=$id, isPick=$isPick, characterName=$characterName, createdAt=$createdAt)"
}

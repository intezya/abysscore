package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.DraftActionType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "draft_actions")
class DraftAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    val draft: MatchDraft

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val actionType: DraftActionType

    @Column(nullable = true)
    val characterName: String?

    @Column(nullable = false)
    val timestamp: LocalDateTime

    @Column(nullable = false)
    val isTimeoutAction: Boolean

    constructor() : this(
        id = 0L,
        draft = MatchDraft(),
        user = User(),
        actionType = DraftActionType.REVEAL_CHARACTERS,
        characterName = null,
        timestamp = LocalDateTime.now(),
        isTimeoutAction = false,
    )

    constructor(
        id: Long = 0L,
        draft: MatchDraft,
        user: User,
        actionType: DraftActionType,
        characterName: String? = null,
        timestamp: LocalDateTime = LocalDateTime.now(),
        isTimeoutAction: Boolean = false,
    ) {
        this.id = id
        this.draft = draft
        this.user = user
        this.actionType = actionType
        this.characterName = characterName
        this.timestamp = timestamp
        this.isTimeoutAction = isTimeoutAction
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DraftAction) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            draft == other.draft &&
                user == other.user &&
                actionType == other.actionType &&
                characterName == other.characterName &&
                timestamp == other.timestamp &&
                isTimeoutAction == other.isTimeoutAction
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(draft, user, actionType, characterName, timestamp, isTimeoutAction)
    }

    override fun toString(): String =
        "DraftAction(id=$id, actionType=$actionType, characterName=$characterName, timestamp=$timestamp, isTimeoutAction=$isTimeoutAction)"
}

package com.intezya.abysscore.model.entity.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.dto.draft.DraftStep
import com.intezya.abysscore.model.entity.match.Match
import jakarta.persistence.*
import java.time.LocalDateTime

const val TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS = 60L
const val TIME_FOR_PERFORM_ACTION_IN_SECONDS = 45L

val DEFAULT_DRAFT_SCHEMA = listOf(
    // bbbb pppp pppp bb pppp pppp - 3 bans + 8 picks per player
    // 1212 1221 1221 21 2112 2112 - 11 actions per player
    DraftStep(firstPlayer = true, isPick = false),
    DraftStep(firstPlayer = false, isPick = false),
    DraftStep(firstPlayer = true, isPick = false),
    DraftStep(firstPlayer = false, isPick = false),

    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),

    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),

    DraftStep(firstPlayer = false, isPick = false),
    DraftStep(firstPlayer = true, isPick = false),

    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),

    DraftStep(firstPlayer = false, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
)

@Entity
@Table(name = "match_drafts")
class MatchDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var currentState: DraftState = DraftState.CHARACTER_REVEAL

    @Column(nullable = false)
    var currentStateStartTime: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var currentStepIndex: Int = 0

    @Column(nullable = false)
    var isPlayer1Ready: Boolean = false

    @Column(nullable = false)
    var isPlayer2Ready: Boolean = false

    @OneToMany(mappedBy = "draft", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val draftActions: MutableList<DraftAction> = mutableListOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "draft_banned_characters", joinColumns = [JoinColumn(name = "draft_id")])
    val bannedCharacters: MutableSet<String> = mutableSetOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "draft_player1_characters", joinColumns = [JoinColumn(name = "draft_id")])
    var player1Characters: MutableSet<String> = mutableSetOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "draft_player2_characters", joinColumns = [JoinColumn(name = "draft_id")])
    var player2Characters: MutableSet<String> = mutableSetOf()

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "draft_player1_available_characters",
        joinColumns = [JoinColumn(name = "draft_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")],
    )
    val player1AvailableCharacters: MutableSet<DraftCharacter> = mutableSetOf()

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(
        name = "draft_player2_available_characters",
        joinColumns = [JoinColumn(name = "draft_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")],
    )
    val player2AvailableCharacters: MutableSet<DraftCharacter> = mutableSetOf()

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    lateinit var match: Match

    constructor()

    @PrePersist
    fun onPersist() {
        currentStateStartTime = LocalDateTime.now()
    }

    @Transient
    fun getDraftSteps(): List<DraftStep> = DEFAULT_DRAFT_SCHEMA

    fun getCurrentStep(): DraftStep? {
        if (currentState != DraftState.DRAFTING) return null
        val steps = getDraftSteps()
        return if (currentStepIndex < steps.size) steps[currentStepIndex] else null
    }

    fun moveToNextStep() {
        val steps = getDraftSteps()
        currentStepIndex++
        if (currentStepIndex >= steps.size) {
            currentState = DraftState.COMPLETED
        }
        currentStateStartTime = LocalDateTime.now()
    }

    fun isCurrentTurnPlayer1(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == true

    fun isCurrentTurnPlayer2(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == false

    fun isCurrentStepPick(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.isPick == true

    fun bothPlayersReady(): Boolean = isPlayer1Ready && isPlayer2Ready

    fun isCompleted(): Boolean = currentStepIndex >= getDraftSteps().size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchDraft) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "MatchDraft(id=$id, currentState=$currentState, currentStepIndex=$currentStepIndex)"
}

package com.intezya.abysscore.model.entity.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.dto.draft.DraftStep
import com.intezya.abysscore.model.entity.match.Match
import jakarta.persistence.*
import java.time.LocalDateTime

const val TIME_FOR_PLAYERS_READY_IN_SECONDS = 60L
const val TIME_FOR_PERFORM_ACTION_IN_SECONDS = 90L // TODO: move to config

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

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "draft_player1_characters",
        joinColumns = [JoinColumn(name = "draft_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")],
    )
    var player1Characters: MutableSet<DraftCharacter> = mutableSetOf()

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinTable(
        name = "draft_player2_characters",
        joinColumns = [JoinColumn(name = "draft_id")],
        inverseJoinColumns = [JoinColumn(name = "character_id")],
    )
    var player2Characters: MutableSet<DraftCharacter> = mutableSetOf()

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

    @get:Transient
    val steps get() = MatchDraftSchema.DRAFT_SCHEMA

    @get:Transient
    val stepsSize get() = MatchDraftSchema.DRAFT_SCHEMA_SIZE

    fun getCurrentStep(): DraftStep? {
        if (currentState != DraftState.DRAFTING) return null
        return if (currentStepIndex < stepsSize) steps[currentStepIndex] else null
    }

    fun moveToNextStep() {
        currentStepIndex++
        if (currentStepIndex >= stepsSize) {
            currentState = DraftState.COMPLETED
        }
        currentStateStartTime = LocalDateTime.now()
    }

    @get:Transient
    val currentStateDeadline: LocalDateTime
        get() = when (currentState) {
            DraftState.CHARACTER_REVEAL -> currentStateStartTime.plusSeconds(TIME_FOR_PLAYERS_READY_IN_SECONDS)
            DraftState.DRAFTING -> currentStateStartTime.plusSeconds(TIME_FOR_PERFORM_ACTION_IN_SECONDS)
            else -> LocalDateTime.MAX
        }

    fun isCurrentTurnPlayer1(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == true

    fun isCurrentTurnPlayer2(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == false

    fun isCurrentStepPick(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.isPick == true

    fun bothPlayersReady(): Boolean = isPlayer1Ready && isPlayer2Ready

    fun isCompleted(): Boolean = currentStepIndex >= stepsSize

    @get:Transient
    val player1PickedCharacters: Set<DraftCharacter>
        get() = draftActions
            .filter { it.isPick && it.player == match.player1 }
            .mapNotNull { action ->
                player1Characters.find { it.name == action.characterName }
            }
            .toSet()

    @get:Transient
    val player2PickedCharacters: Set<DraftCharacter>
        get() = draftActions
            .filter { it.isPick && it.player == match.player2 }
            .mapNotNull { action ->
                player2Characters.find { it.name == action.characterName }
            }
            .toSet()

    @get:Transient
    val bannedCharacters: Set<DraftCharacter>
        get() = draftActions
            .filter { !it.isPick }
            .mapNotNull { action ->
                player1Characters.find { it.name == action.characterName }
                    ?: player2Characters.find { it.name == action.characterName }
            }
            .toSet()

    @get:Transient
    val player1AvailableCharacters: Set<DraftCharacter>
        get() = player1Characters
            .filter { character ->
                !player2PickedCharacters.any { it.name == character.name } &&
                    !bannedCharacters.any { it.name == character.name }
            }
            .toSet()

    @get:Transient
    val player2AvailableCharacters: Set<DraftCharacter>
        get() = player2Characters
            .filter { character ->
                !player1PickedCharacters.any { it.name == character.name } &&
                    !bannedCharacters.any { it.name == character.name }
            }
            .toSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchDraft) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "MatchDraft(id=$id, currentState=$currentState, currentStepIndex=$currentStepIndex)"
}

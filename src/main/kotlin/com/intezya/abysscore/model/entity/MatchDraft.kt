package com.intezya.abysscore.model.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.dto.draft.DraftStep
import jakarta.persistence.*
import java.time.LocalDateTime
import kotlin.jvm.Transient

// TODO
val DEFAULT_DRAFT_SCHEMA = listOf(
    DraftStep(firstPlayer = true, isPick = false),
    DraftStep(firstPlayer = false, isPick = false),
    DraftStep(firstPlayer = true, isPick = true),
    DraftStep(firstPlayer = false, isPick = true),
)


const val TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS = 60L
const val TIME_FOR_PERFORM_ACTION_IN_SECONDS = 45L

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
    var penaltyTimePlayer1: Int = 0

    @Column(nullable = false)
    var penaltyTimePlayer2: Int = 0

    @Column(nullable = false)
    var currentStateDeadline: LocalDateTime = LocalDateTime.now().plusMinutes(5)

    @Column(nullable = false)
    var isPlayer1Ready: Boolean = false

    @Column(nullable = false)
    var isPlayer2Ready: Boolean = false

    @Column(columnDefinition = "TEXT")
    var draftSchemaJson: String = "[]"

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

    @Transient
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }

    constructor()

    @PrePersist
    fun onPersist() {
        currentStateStartTime = LocalDateTime.now()
        currentStateDeadline = calculateDeadline()
    }

    fun getDraftSteps(): List<DraftStep> = try {
        objectMapper.readValue(draftSchemaJson, object : TypeReference<List<DraftStep>>() {})
    } catch (e: Exception) {
        e.printStackTrace()
        DEFAULT_DRAFT_SCHEMA
    }

    fun setDraftSteps(steps: List<DraftStep>) {
        draftSchemaJson = objectMapper.writeValueAsString(steps)
    }

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
        currentStateDeadline = calculateDeadline()
    }

    fun calculateDeadline(): LocalDateTime {
        val baseTimeout = when (currentState) {
            DraftState.CHARACTER_REVEAL -> TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS
            DraftState.DRAFTING -> TIME_FOR_PERFORM_ACTION_IN_SECONDS
            else -> 60
        }

        val additionalTime = when {
            currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == true -> penaltyTimePlayer1
            currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == false -> penaltyTimePlayer2
            else -> 0
        }

        return LocalDateTime.now().plusSeconds((baseTimeout + additionalTime).toLong())
    }

    fun isCurrentTurnPlayer1(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == true

    fun isCurrentTurnPlayer2(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.firstPlayer == false

    fun isCurrentStepPick(): Boolean = currentState == DraftState.DRAFTING && getCurrentStep()?.isPick == true

    fun bothPlayersReady() = this.isPlayer1Ready && this.isPlayer2Ready

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatchDraft) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "MatchDraft(id=$id, currentState=$currentState, currentStepIndex=$currentStepIndex)"
}

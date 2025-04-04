package com.intezya.abysscore.model.dto.draft

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.MatchDraft
import java.time.LocalDateTime

data class MatchDraftDTO(
    val currentState: DraftState,
    val currentStateStartTime: LocalDateTime,
    val currentStepIndex: Int,
    val penaltyTimePlayer1: Int,
    val penaltyTimePlayer2: Int,
    val currentStateDeadline: LocalDateTime,
    val isPlayer1Ready: Boolean,
    val isPlayer2Ready: Boolean,
    @field:JsonProperty("draft_schema")
    val draftSchemaJson: List<DraftStep>,
    val draftActions: List<DraftActionDTO>,
    val bannedCharacters: Set<String>,
    val player1Characters: Set<String>,
    val player2Characters: Set<String>,
    val player1AvailableCharacters: Set<DraftCharacterDTO>,
    val player2AvailableCharacters: Set<DraftCharacterDTO>,
    val createdAt: LocalDateTime,
) {
    constructor(matchDraft: MatchDraft) : this(
        currentState = matchDraft.currentState,
        currentStateStartTime = matchDraft.currentStateStartTime,
        currentStepIndex = matchDraft.currentStepIndex,
        penaltyTimePlayer1 = matchDraft.penaltyTimePlayer1,
        penaltyTimePlayer2 = matchDraft.penaltyTimePlayer2,
        currentStateDeadline = matchDraft.currentStateDeadline,
        isPlayer1Ready = matchDraft.isPlayer1Ready,
        isPlayer2Ready = matchDraft.isPlayer2Ready,
        draftSchemaJson = matchDraft.getDraftSteps(),
        draftActions = matchDraft.draftActions.map { it.toDTO() },
        bannedCharacters = matchDraft.bannedCharacters,
        player1Characters = matchDraft.player1Characters,
        player2Characters = matchDraft.player2Characters,
        player1AvailableCharacters = matchDraft.player1AvailableCharacters.map { it.toDTO() }.toSet(),
        player2AvailableCharacters = matchDraft.player2AvailableCharacters.map { it.toDTO() }.toSet(),
        createdAt = matchDraft.createdAt,
    )
}

fun MatchDraft.toDTO(): MatchDraftDTO = MatchDraftDTO(this)

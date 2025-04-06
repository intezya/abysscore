package com.intezya.abysscore.model.dto.draft

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.CharacterData
import com.intezya.abysscore.model.entity.draft.MatchDraft
import java.time.LocalDateTime

data class MatchDraftDTO(
    val currentState: DraftState,
    val currentStateStartTime: LocalDateTime,
    val currentStepIndex: Int,
    val isPlayer1Ready: Boolean,
    val isPlayer2Ready: Boolean,
    @field:JsonProperty("draft_schema")
    val draftSchemaJson: List<DraftStep>,
    val draftActions: List<DraftActionDTO>,
    val bannedCharacters: List<String>,
    val player1Characters: Set<CharacterData>,
    val player2Characters: Set<CharacterData>,
    val player1PickedCharacters: Set<String>,
    val player2PickedCharacters: Set<String>,
    val createdAt: LocalDateTime,
) {
    constructor(draft: MatchDraft) : this(
        currentState = draft.currentState,
        currentStateStartTime = draft.currentStateStartTime,
        currentStepIndex = draft.currentStepIndex,
        isPlayer1Ready = draft.isPlayer1Ready,
        isPlayer2Ready = draft.isPlayer2Ready,
        draftSchemaJson = draft.steps,
        draftActions = draft.draftActions.map { it.toDTO() },
        bannedCharacters = draft.draftActions.filter { !it.isPick }.map { it.characterName },
        player1Characters = draft.player1Characters,
        player2Characters = draft.player2Characters,
        player1PickedCharacters = draft.player1PickedCharacters,
        player2PickedCharacters = draft.player2PickedCharacters,
        createdAt = draft.createdAt,
    )
}

fun MatchDraft.toDTO(): MatchDraftDTO = MatchDraftDTO(this)

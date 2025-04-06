package com.intezya.abysscore.model.dto.draft

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.DEFAULT_DRAFT_SCHEMA
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
        isPlayer1Ready = matchDraft.isPlayer1Ready,
        isPlayer2Ready = matchDraft.isPlayer2Ready,
        draftSchemaJson = DEFAULT_DRAFT_SCHEMA,
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

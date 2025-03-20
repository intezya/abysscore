package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.MatchDraft
import java.time.LocalDateTime

data class MatchDraftDTO(
    var currentState: DraftState,
    var currentStateStartTime: LocalDateTime,
    var currentStepIndex: Int,
    var penaltyTimePlayer1: Int,
    var penaltyTimePlayer2: Int,
    var currentStateDeadline: LocalDateTime,
    var isPlayer1Ready: Boolean,
    var isPlayer2Ready: Boolean,
    var draftSchemaJson: String,
    val draftActions: List<DraftActionDTO>,
    val bannedCharacters: Set<String>,
    var player1Characters: Set<String>,
    var player2Characters: Set<String>,
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
        draftSchemaJson = matchDraft.draftSchemaJson,
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

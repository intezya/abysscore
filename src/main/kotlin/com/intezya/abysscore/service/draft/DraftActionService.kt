package com.intezya.abysscore.service.draft

import com.intezya.abysscore.event.draftprocess.DraftActionPerformEvent
import com.intezya.abysscore.model.dto.draft.MatchDraftWithDraftAction
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.DraftAction
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.DraftActionRepository
import com.intezya.abysscore.repository.MatchDraftRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class DraftActionService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftActionRepository: DraftActionRepository,
    private val draftValidationService: DraftValidationService,
    private val eventPublisher: ApplicationEventPublisher,
    private val draftCompletionService: DraftCompletionService,
) {
    fun performDraftAction(user: User, characterName: String): MatchDraft {
        val match = user.currentMatch ?: throw IllegalStateException("User is not in a match")
        val draft = match.draft

        val playerInfo = draftValidationService.getPlayerInfo(match, user.id)
        draftValidationService.validateUserTurn(draft, playerInfo.isPlayer1)

        val result = if (draft.isCurrentStepPick()) {
            pickCharacter(draft, playerInfo, characterName)
        } else {
            banCharacter(draft, playerInfo, characterName)
        }

        eventPublisher.publishEvent(DraftActionPerformEvent(this, user, match, result.draftAction))

        if (draft.isCompleted()) {
            draftCompletionService.completeDraft(draft)
        }

        return result.draft
    }

    fun saveDraftAction(
        draft: MatchDraft,
        player: User,
        characterName: String,
        isPick: Boolean,
        stepIndex: Int,
    ): DraftAction {
        val action = DraftAction(
            draft = draft,
            player = player,
            characterName = characterName,
            isPick = isPick,
            stepIndex = stepIndex,
        )

        return draftActionRepository.save(action)
    }

    internal fun banCharacter(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characterName: String,
    ): MatchDraftWithDraftAction {
        checkBanConditions(draft, characterName)

        val draftAction = saveDraftAction(
            draft = draft,
            player = playerInfo.player,
            characterName = characterName,
            isPick = false,
            stepIndex = draft.currentStepIndex,
        )

        draft.moveToNextStep()

        return MatchDraftWithDraftAction(matchDraftRepository.save(draft), draftAction)
    }

    internal fun pickCharacter(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characterName: String,
    ): MatchDraftWithDraftAction {
        checkPickConditions(draft, characterName, playerInfo.isPlayer1)

        val draftAction = saveDraftAction(
            draft = draft,
            player = playerInfo.player,
            characterName = characterName,
            isPick = true,
            stepIndex = draft.currentStepIndex,
        )

        draft.moveToNextStep()

        return MatchDraftWithDraftAction(matchDraftRepository.save(draft), draftAction)
    }

    private fun checkPickConditions(draft: MatchDraft, characterName: String, isPlayer1: Boolean) {
        val userCharacters = if (isPlayer1) draft.player1Characters else draft.player2Characters

        if (userCharacters.find { it.name == characterName } == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' not found in your pool",
            )
        }

        if (draft.draftActions.any { it.characterName == characterName }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' is not available for picking",
            )
        }
    }

    private fun checkBanConditions(draft: MatchDraft, characterName: String) {
        val characters = draft.player1Characters + draft.player2Characters

        if (characters.find { it.name == characterName } == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' not found in your pool",
            )
        }

        if (draft.draftActions.any { it.characterName == characterName }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' is not available for banning",
            )
        }
    }
}

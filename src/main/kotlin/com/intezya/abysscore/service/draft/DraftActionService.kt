package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.DraftActionPerformEvent
import com.intezya.abysscore.model.dto.draft.MatchDraftWithDraftAction
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.DraftAction
import com.intezya.abysscore.model.entity.draft.DraftCharacter
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
) {
    fun performDraftAction(user: User, characterName: String): MatchDraft {
        val match =
            draftValidationService.validateMatchStatus(user, MatchStatus.DRAFTING, "Match is not in drafting stage")
        val draft = match.draft

        draftValidationService.validateDraftState(draft, DraftState.DRAFTING)
        val playerInfo = draftValidationService.getPlayerInfo(match, user.id)
        draftValidationService.validateUserTurn(draft, playerInfo.isPlayer1)

        val result = if (draft.isCurrentStepPick()) {
            pickCharacter(draft, playerInfo, characterName)
        } else {
            banCharacter(draft, playerInfo, characterName)
        }

        eventPublisher.publishEvent(DraftActionPerformEvent(this, user, match, result.draftAction))

        return result.draft
    }

    fun logDraftAction(
        draft: MatchDraft,
        player: User,
        actionType: DraftActionType,
        characterName: String? = null,
    ): DraftAction {
        val action = DraftAction(
            draft = draft,
            user = player,
            actionType = actionType,
            characterName = characterName,
        )

        return draftActionRepository.save(action)
    }

    internal fun banCharacter(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characterName: String,
    ): MatchDraftWithDraftAction {
        val isPlayer1 = playerInfo.isPlayer1

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val opponentPool = if (isPlayer1) draft.player2AvailableCharacters else draft.player1AvailableCharacters

        if (!isCharacterAvailableForBanning(opponentPool, characterName)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character not available for banning",
            )
        }

        opponentPool.removeIf { it.name == characterName }
        userPool.removeIf { it.name == characterName }
        draft.bannedCharacters.add(characterName)

        val draftAction = logDraftAction(
            draft = draft,
            player = playerInfo.player,
            actionType = DraftActionType.BAN_CHARACTER,
            characterName = characterName,
        )

        draft.moveToNextStep()

        return MatchDraftWithDraftAction(matchDraftRepository.save(draft), draftAction)
    }

    private fun isCharacterAvailableForBanning(
        characterPool: Collection<DraftCharacter>,
        characterName: String,
    ): Boolean = characterPool.any { it.name == characterName }

    internal fun pickCharacter(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characterName: String,
    ): MatchDraftWithDraftAction {
        val isPlayer1 = playerInfo.isPlayer1

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val userCharacters = if (isPlayer1) draft.player1Characters else draft.player2Characters
        val opponentCharacters = if (isPlayer1) draft.player2Characters else draft.player1Characters

        if (!isCharacterAvailableForPicking(userPool, opponentCharacters, characterName)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' is not available for picking",
            )
        }

        userCharacters.add(characterName)
        userPool.removeIf { it.name == characterName }

        val draftAction = logDraftAction(
            draft = draft,
            player = playerInfo.player,
            actionType = DraftActionType.PICK_CHARACTER,
            characterName = characterName,
        )

        draft.moveToNextStep()

        return MatchDraftWithDraftAction(matchDraftRepository.save(draft), draftAction)
    }

    private fun isCharacterAvailableForPicking(
        userPool: Collection<DraftCharacter>,
        opponentCharacters: Collection<String>,
        characterName: String,
    ): Boolean = userPool.any { it.name == characterName } && !opponentCharacters.contains(characterName)
}

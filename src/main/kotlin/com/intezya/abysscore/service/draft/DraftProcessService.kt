package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.event.draftprocess.DraftActionPerformEvent
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.entity.draft.DraftAction
import com.intezya.abysscore.model.entity.draft.DraftCharacter
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.draft.TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.DraftActionRepository
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
import com.intezya.abysscore.service.MatchProcessService
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime


@Service
@Transactional
class DraftProcessService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftActionRepository: DraftActionRepository,
    private val matchProcessService: MatchProcessService,
    private val matchRepository: MatchRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    // TODO: unit tests
    fun revealCharacters(user: User, characters: List<DraftCharacterDTO>): MatchDraft {
        val match = validateMatchStatus(user, MatchStatus.PENDING, "Match is not in reveal characters stage")
        val draft = match.draft

        if (match.hasPlayerAlreadyRevealedCharacters(user)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already revealed your characters")
        }

        validateDraftState(draft, expectedState = DraftState.CHARACTER_REVEAL)
        val playerInfo = getPlayerInfo(match, user.id)

        registerPlayerCharacters(draft, playerInfo, characters)
        logDraftAction(draft, playerInfo.player, DraftActionType.REVEAL_CHARACTERS)

        if (draft.bothPlayersReady()) {
            advanceToDraftingState(match, draft)
        }

        eventPublisher.publishEvent(CharactersRevealEvent(this, match, user, characters))

        return matchDraftRepository.save(draft)
    }

    // TODO: unit tests
    fun performDraftAction(user: User, characterName: String): MatchDraft {
        val match = validateMatchStatus(user, MatchStatus.DRAFTING, "Match is not in drafting stage")
        val draft = match.draft

        validateDraftState(draft, DraftState.DRAFTING)
        val playerInfo = getPlayerInfo(match, user.id)
        validateUserTurn(draft, playerInfo.isPlayer1)

        val result = if (draft.isCurrentStepPick()) {
            pickCharacter(draft, playerInfo, characterName)
        } else {
            banCharacter(draft, playerInfo, characterName)
        }

        eventPublisher.publishEvent(DraftActionPerformEvent(this, user, match, result.draftAction))

        return result.draft
    }

    @Scheduled(fixedRate = 1000)
    fun checkTimeouts() {
        val now = LocalDateTime.now()
        val expiredDrafts = matchDraftRepository.findByCurrentStateNotAndCurrentStateDeadlineBefore(
            DraftState.COMPLETED,
            now,
        )

        expiredDrafts.forEach { handleExpiredDraft(it) }
    }

    private fun registerPlayerCharacters(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characters: List<DraftCharacterDTO>,
    ) {
        val characterEntities = characters.map { it.toEntity() }

        if (playerInfo.isPlayer1) {
            draft.player1AvailableCharacters.addAll(characterEntities)
            draft.isPlayer1Ready = true
        } else {
            draft.player2AvailableCharacters.addAll(characterEntities)
            draft.isPlayer2Ready = true
        }

        // TODO: add saved characters logging for statistics and analysis
    }

    private fun logDraftAction(
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

    private fun advanceToDraftingState(match: Match, draft: MatchDraft) {
        match.status = MatchStatus.DRAFTING
        matchRepository.save(match)

        advanceDraftToDraftingState(draft)
    }

    private fun banCharacter(
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
    ): Boolean {
        return characterPool.any { it.name == characterName }
    }

    private fun pickCharacter(
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
    ): Boolean {
        return userPool.any { it.name == characterName } && !opponentCharacters.contains(characterName)
    }

    private fun handleExpiredDraft(draft: MatchDraft) {
        logger.info("Processing expired draft: ${draft.id}")

        when (draft.currentState) {
            DraftState.CHARACTER_REVEAL -> handleExpiredCharacterReveal(draft)
            DraftState.DRAFTING -> handleExpiredDrafting(draft)
            else -> {}
        }

        if (isDraftCompleted(draft)) {
            completeDraft(draft)
        }
    }

    private fun isDraftCompleted(draft: MatchDraft): Boolean {
        return draft.currentStepIndex >= draft.draftActions.size
    }

    private fun handleExpiredCharacterReveal(draft: MatchDraft) {
        // TODO: don't update players statistics
        matchProcessService.checkPlayerTimeouts(
            draft.match,
            timeoutThreshold = Duration.ofSeconds(TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS),
            playerResults = mapOf(
                draft.match.player1 to draft.currentStateStartTime,
                draft.match.player2 to draft.currentStateStartTime,
            ),
        )
    }

    private fun handleExpiredDrafting(draft: MatchDraft) {
        val isPlayer1Turn = draft.isCurrentTurnPlayer1()
        val availablePool = if (isPlayer1Turn) {
            draft.player1AvailableCharacters
        } else {
            draft.player2AvailableCharacters
        }

        if (availablePool.isNotEmpty()) {
            performAutomaticDraftAction(draft, isPlayer1Turn, availablePool)
        } else {
            draft.moveToNextStep()
            matchDraftRepository.save(draft)
        }
    }

    private fun performAutomaticDraftAction(
        draft: MatchDraft,
        isPlayer1Turn: Boolean,
        availablePool: Set<DraftCharacter>,
    ) {
        val randomCharacter = availablePool.random()
        val player = if (isPlayer1Turn) draft.match.player1 else draft.match.player2
        val playerInfo = PlayerInfo(player, isPlayer1Turn)

        if (draft.isCurrentStepPick()) {
            pickCharacter(draft, playerInfo, randomCharacter.name)
        } else {
            banCharacter(draft, playerInfo, randomCharacter.name)
        }

        val action = if (draft.isCurrentStepPick()) "picked" else "banned"
        logger.info("Auto-$action character ${randomCharacter.name} for timeout in draft ${draft.id}")

        // TODO: notifyBothPlayers(draft, DraftNotificationType.AUTO_SELECTION, randomCharacter.name)
    }

    private fun completeDraft(draft: MatchDraft) {
        draft.currentState = DraftState.COMPLETED
        matchDraftRepository.save(draft)

        // TODO: notifyBothPlayers(draft, DraftNotificationType.DRAFT_COMPLETED)
    }

    private fun validateMatchStatus(user: User, expectedStatus: MatchStatus, errorMessage: String): Match {
        val match = user.currentMatch ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in a match")

        if (match.status != expectedStatus) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        return match
    }

    private fun validateDraftState(draft: MatchDraft, expectedState: DraftState) {
        if (draft.currentState != expectedState) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Not in needed phase. Current state: ${draft.currentState}",
            )
        }
    }

    private fun validateUserTurn(draft: MatchDraft, isPlayer1: Boolean) {
        val isUserTurn = (isPlayer1 && draft.isCurrentTurnPlayer1()) || (!isPlayer1 && draft.isCurrentTurnPlayer2())
        if (!isUserTurn) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Not your turn to make a move",
            )
        }
    }

    private fun getPlayerInfo(match: Match, userId: Long): PlayerInfo {
        val isPlayer1 = match.player1.id == userId
        val isPlayer2 = match.player2.id == userId

        if (!isPlayer1 && !isPlayer2) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User with ID $userId is not part of this match",
            )
        }

        return PlayerInfo(
            player = if (isPlayer1) match.player1 else match.player2,
            isPlayer1 = isPlayer1,
        )
    }

    private fun advanceDraftToDraftingState(draft: MatchDraft) {
        draft.currentState = DraftState.DRAFTING
        draft.currentStateStartTime = LocalDateTime.now()
        draft.currentStateDeadline = draft.calculateDeadline()
    }

    private data class PlayerInfo(val player: User, val isPlayer1: Boolean)

    private data class MatchDraftWithDraftAction(
        val draft: MatchDraft,
        val draftAction: DraftAction,
    )
}

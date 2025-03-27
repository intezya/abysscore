package com.intezya.abysscore.service

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.entity.DraftAction
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.MatchDraft
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.DraftActionRepository
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
import org.apache.commons.logging.LogFactory
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
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun revealCharacters(user: User, characters: List<DraftCharacterDTO>): MatchDraft {
        val match = validateMatchStatus(user, MatchStatus.PENDING, "Match is not in reveal characters stage")
        val draft = match.draft

        if (match.player1.id == user.id && draft.player1AvailableCharacters.isNotEmpty() || match.player2.id == user.id && draft.player2AvailableCharacters.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already revealed your characters")
        }

        validateDraftState(draft, expectedState = DraftState.CHARACTER_REVEAL)
        val playerInfo = getPlayerInfo(match, user.id)

        registerPlayerCharacters(draft, playerInfo, characters)

        logDraftAction(draft, playerInfo.player, DraftActionType.REVEAL_CHARACTERS)

        if (draft.isPlayer1Ready && draft.isPlayer2Ready) {
            advanceToDraftingState(match, draft)
        }

        notifyOpponent(draft, playerInfo.isPlayer1, DraftNotificationType.CHARACTERS_REVEALED)

        return matchDraftRepository.save(draft)
    }

    fun performDraftAction(user: User, characterName: String): MatchDraft {
        val match = validateMatchStatus(user, MatchStatus.DRAFTING, "Match is not in drafting stage")
        val draft = match.draft

        validateDraftState(draft, DraftState.DRAFTING)
        val playerInfo = getPlayerInfo(match, user.id)
        validateUserTurn(draft, playerInfo.isPlayer1)

        val isPick = draft.isCurrentStepPick()
        val result = if (isPick) {
            pickCharacter(draft, playerInfo, characterName)
        } else {
            banCharacter(draft, playerInfo, characterName)
        }

        val notificationType = if (isPick) {
            DraftNotificationType.CHARACTER_PICKED
        } else {
            DraftNotificationType.CHARACTER_BANNED
        }

        notifyOpponent(draft, playerInfo.isPlayer1, notificationType, characterName)

        return result
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

    private fun banCharacter(draft: MatchDraft, playerInfo: PlayerInfo, characterName: String): MatchDraft {
        val isPlayer1 = playerInfo.isPlayer1

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val opponentPool = if (isPlayer1) draft.player2AvailableCharacters else draft.player1AvailableCharacters

        if (!opponentPool.any { it.name == characterName }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character not available for banning",
            )
        }

        opponentPool.removeIf { it.name == characterName }
        userPool.removeIf { it.name == characterName }
        draft.bannedCharacters.add(characterName)

        logDraftAction(
            draft = draft,
            player = playerInfo.player,
            actionType = DraftActionType.BAN_CHARACTER,
            characterName = characterName,
        )

        draft.moveToNextStep()

        return matchDraftRepository.save(draft)
    }

    private fun pickCharacter(draft: MatchDraft, playerInfo: PlayerInfo, characterName: String): MatchDraft {
        val isPlayer1 = playerInfo.isPlayer1

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val userCharacters = if (isPlayer1) draft.player1Characters else draft.player2Characters
        val opponentCharacters = if (isPlayer1) draft.player2Characters else draft.player1Characters

        if (!userPool.any { it.name == characterName } || opponentCharacters.contains(characterName)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Character '$characterName' is not available for picking",
            )
        }

        userCharacters.add(characterName)
        userPool.removeIf { it.name == characterName }

        logDraftAction(
            draft = draft,
            player = playerInfo.player,
            actionType = DraftActionType.PICK_CHARACTER,
            characterName = characterName,
        )

        draft.moveToNextStep()

        return matchDraftRepository.save(draft)
    }

    private fun handleExpiredDraft(draft: MatchDraft) {
        logger.info("Processing expired draft: ${draft.id}")

        when (draft.currentState) {
            DraftState.CHARACTER_REVEAL -> handleExpiredCharacterReveal(draft)
            DraftState.DRAFTING -> handleExpiredDrafting(draft)
            else -> {}
        }

        if (draft.currentStepIndex >= draft.draftActions.size) {
            completeDraft(draft)
        }
    }

    private fun handleExpiredCharacterReveal(draft: MatchDraft) {
        matchProcessService.checkPlayerTimeouts(
            draft.match,
            Duration.ofMinutes(1),
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

            notifyBothPlayers(draft, DraftNotificationType.AUTO_SELECTION, randomCharacter.name)
        } else {
            draft.moveToNextStep()
            matchDraftRepository.save(draft)
        }
    }

    private fun completeDraft(draft: MatchDraft) {
        draft.currentState = DraftState.COMPLETED
        matchDraftRepository.save(draft)

        notifyBothPlayers(draft, DraftNotificationType.DRAFT_COMPLETED)
    }

    private fun notifyBothPlayers(
        draft: MatchDraft,
        notificationType: DraftNotificationType,
        characterName: String? = null,
    ) {
        notifyOpponent(draft, true, notificationType, characterName)
        notifyOpponent(draft, false, notificationType, characterName)
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

    private fun notifyOpponent(
        draft: MatchDraft,
        isActingPlayerOne: Boolean,
        notificationType: DraftNotificationType,
        characterName: String? = null,
    ) {
        try {
            val match = draft.match
            val opponentId = if (isActingPlayerOne) match.player2.id else match.player1.id

            // TODO
            DraftNotification(
                draftId = draft.id,
                type = notificationType,
                characterName = characterName,
                currentState = draft.currentState,
                currentStepIndex = draft.currentStepIndex,
                deadline = draft.currentStateDeadline,
            )

            // websocketService.sendToUser(
            //     userId = opponentId,
            //     destination = "/topic/draft.${draft.id}",
            //     payload = objectMapper.writeValueAsString(notification)
            // )

            logger.debug("Notification sent to user $opponentId: $notificationType")
        } catch (e: Exception) {
            logger.error("Failed to send notification for draft ${draft.id}", e)
        }
    }

    private fun advanceDraftToDraftingState(draft: MatchDraft) {
        draft.currentState = DraftState.DRAFTING
        draft.currentStateStartTime = LocalDateTime.now()
        draft.currentStateDeadline = draft.calculateDeadline()
    }

    private data class PlayerInfo(val player: User, val isPlayer1: Boolean)

    private data class DraftNotification(
        val draftId: Long,
        val type: DraftNotificationType,
        val characterName: String?,
        val currentState: DraftState,
        val currentStepIndex: Int,
        val deadline: LocalDateTime,
    )

    private enum class DraftNotificationType {
        CHARACTERS_REVEALED,
        CHARACTER_PICKED,
        CHARACTER_BANNED,
        AUTO_SELECTION,
        DRAFT_COMPLETED,
    }
}

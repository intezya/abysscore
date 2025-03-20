package com.intezya.abysscore.service

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.*
import com.intezya.abysscore.repository.DraftActionRepository
import com.intezya.abysscore.repository.MatchDraftRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.commons.logging.LogFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class DraftProcessService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftActionRepository: DraftActionRepository,
    private val matchProcessService: MatchProcessService,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun revealCharacters(draftId: Long, userId: Long, characters: List<DraftCharacter>): MatchDraft {
        val draft = findDraftById(draftId)
        validateDraftState(draft, expectedState = DraftState.CHARACTER_REVEAL)

        val match = draft.match
        val playerInfo = getPlayerInfo(match, userId)

        if (playerInfo.isPlayer1) {
            draft.player1AvailableCharacters.addAll(characters)
            draft.isPlayer1Ready = true
        } else {
            draft.player2AvailableCharacters.addAll(characters)
            draft.isPlayer2Ready = true
        }

        val action = DraftAction(
            draft = draft,
            user = playerInfo.player,
            actionType = DraftActionType.REVEAL_CHARACTERS,
        )

        // TODO: add saved characters logging for statistics and analysis

        draftActionRepository.save(action)

        if (draft.isPlayer1Ready && draft.isPlayer2Ready) {
            advanceDraftToNextState(draft)
        }

        notifyOpponent(draft, playerInfo.isPlayer1, DraftNotificationType.CHARACTERS_REVEALED)

        return matchDraftRepository.save(draft)
    }

    fun performDraftAction(draftId: Long, userId: Long, characterName: String): MatchDraft {
        val draft = findDraftById(draftId)
        validateDraftState(draft, DraftState.DRAFTING)

        val match = draft.match
        val playerInfo = getPlayerInfo(match, userId)

        validateUserTurn(draft, playerInfo.isPlayer1)

        val isPick = draft.isCurrentStepPick()
        val result = if (isPick) {
            pickCharacter(draft, userId, characterName)
        } else {
            banCharacter(draft, userId, characterName)
        }

        val notificationType =
            if (isPick) DraftNotificationType.CHARACTER_PICKED else DraftNotificationType.CHARACTER_BANNED
        notifyOpponent(draft, playerInfo.isPlayer1, notificationType, characterName)

        return result
    }

    private fun banCharacter(draft: MatchDraft, userId: Long, characterName: String): MatchDraft {
        val match = draft.match
        val isPlayer1 = match.player1.id == userId

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val opponentPool = if (isPlayer1) draft.player2AvailableCharacters else draft.player1AvailableCharacters

        if (!opponentPool.any { it.name == characterName }) {
            throw IllegalArgumentException("Character not available for banning")
        }

        opponentPool.removeIf { it.name == characterName }
        userPool.removeIf { it.name == characterName }
        draft.bannedCharacters.add(characterName)

        val action = DraftAction(
            draft = draft,
            user = if (isPlayer1) match.player1 else match.player2,
            actionType = DraftActionType.BAN_CHARACTER,
            characterName = characterName,
        )
        draftActionRepository.save(action)

        draft.moveToNextStep()

        return matchDraftRepository.save(draft)
    }

    private fun pickCharacter(draft: MatchDraft, userId: Long, characterName: String): MatchDraft {
        val match = draft.match
        val isPlayer1 = match.player1.id == userId

        val userPool = if (isPlayer1) draft.player1AvailableCharacters else draft.player2AvailableCharacters
        val userCharacters = if (isPlayer1) draft.player1Characters else draft.player2Characters
        val opponentCharacters = if (isPlayer1) draft.player2Characters else draft.player1Characters

        if (!userPool.any { it.name == characterName } || opponentCharacters.contains(characterName)) {
            throw IllegalArgumentException("Character '$characterName' is not available for picking")
        }

        userCharacters.add(characterName)
        userPool.removeIf { it.name == characterName }

        val action = DraftAction(
            draft = draft,
            user = if (isPlayer1) match.player1 else match.player2,
            actionType = DraftActionType.PICK_CHARACTER,
            characterName = characterName,
        )
        draftActionRepository.save(action)

        draft.moveToNextStep()

        return matchDraftRepository.save(draft)
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

    private fun findDraftById(draftId: Long): MatchDraft = matchDraftRepository.findById(draftId)
        .orElseThrow {
            EntityNotFoundException("Draft with ID $draftId not found")
        }

    private fun validateDraftState(draft: MatchDraft, expectedState: DraftState) {
        if (draft.currentState != expectedState) {
            throw IllegalStateException("Not in needed phase. Current state: ${draft.currentState}")
        }
    }

    private fun validateUserTurn(draft: MatchDraft, isPlayer1: Boolean) {
        val isUserTurn = (isPlayer1 && draft.isCurrentTurnPlayer1()) || (!isPlayer1 && draft.isCurrentTurnPlayer2())
        if (!isUserTurn) {
            throw IllegalStateException("Not your turn to make a move")
        }
    }

    private fun getPlayerInfo(match: Match, userId: Long): PlayerInfo {
        val isPlayer1 = match.player1.id == userId
        val isPlayer2 = match.player2.id == userId

        if (!isPlayer1 && !isPlayer2) {
            throw IllegalArgumentException("User with ID $userId is not part of this match")
        }

        return PlayerInfo(
            player = if (isPlayer1) match.player1 else match.player2,
            isPlayer1 = isPlayer1,
        )
    }

    private fun handleExpiredDraft(draft: MatchDraft) {
        logger.info("Processing expired draft: ${draft.id}")

        if (draft.currentState == DraftState.CHARACTER_REVEAL) {
            // TODO
            matchProcessService.checkPlayerTimeouts(
                draft.match,
                Duration.ofMinutes(1),
                playerResults = mapOf(
                    draft.match.player1 to draft.currentStateStartTime,
                    draft.match.player2 to draft.currentStateStartTime,
                ),
            )
            return
        }

        if (draft.currentState == DraftState.DRAFTING) {
            val isPlayer1Turn = draft.isCurrentTurnPlayer1()
            val availablePool =
                if (isPlayer1Turn) draft.player1AvailableCharacters else draft.player2AvailableCharacters

            if (availablePool.isNotEmpty()) {
                val randomCharacter = availablePool.random()
                val userId = if (isPlayer1Turn) draft.match.player1.id else draft.match.player2.id

                if (draft.isCurrentStepPick()) {
                    pickCharacter(draft, userId, randomCharacter.name)
                } else {
                    banCharacter(draft, userId, randomCharacter.name)
                }

                val action = if (draft.isCurrentStepPick()) "picked" else "banned"

                logger.info("Auto-$action character ${randomCharacter.name} for timeout in draft ${draft.id}")

                notifyOpponent(draft, true, DraftNotificationType.AUTO_SELECTION, randomCharacter.name)
                notifyOpponent(draft, false, DraftNotificationType.AUTO_SELECTION, randomCharacter.name)
            } else {
                draft.moveToNextStep()
                matchDraftRepository.save(draft)
            }
        }

        if (draft.currentStepIndex >= draft.draftActions.size) {
            draft.currentState = DraftState.COMPLETED
            matchDraftRepository.save(draft)

            notifyOpponent(draft, true, DraftNotificationType.DRAFT_COMPLETED)
            notifyOpponent(draft, false, DraftNotificationType.DRAFT_COMPLETED)
        }
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

            val notification = DraftNotification(
                draftId = draft.id,
                type = notificationType,
                characterName = characterName,
                currentState = draft.currentState,
                currentStepIndex = draft.currentStepIndex,
                deadline = draft.currentStateDeadline,
            )
            // TODO:

//            websocketService.sendToUser(
//                userId = opponentId,
//                destination = "/topic/draft.${draft.id}",
//                payload = objectMapper.writeValueAsString(notification)
//            )
        } catch (e: Exception) {
            logger.error("Failed to send notification for draft ${draft.id}", e)
        }
    }

    private fun advanceDraftToNextState(draft: MatchDraft) {
        draft.currentState = DraftState.DRAFTING
        draft.currentStateStartTime = LocalDateTime.now()
        draft.currentStateDeadline = draft.calculateDeadline()
    }

    private data class PlayerInfo(
        val player: User,
        val isPlayer1: Boolean,
    )

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

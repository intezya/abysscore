package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.event.draftprocess.AutomaticDraftActionPerformEvent
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.DraftCharacter
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.draft.TIME_FOR_CHARACTERS_REVEAL_IN_SECONDS
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.service.MatchProcessService
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
@Transactional
class DraftTimeoutService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftActionService: DraftActionService,
    private val matchProcessService: MatchProcessService,
    private val draftCompletionService: DraftCompletionService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    @Scheduled(fixedRate = 1000)
    fun checkTimeouts() {
        val now = LocalDateTime.now()
        val expiredDrafts = matchDraftRepository.findByCurrentStateNotAndCurrentStateDeadlineBefore(
            DraftState.COMPLETED,
            now,
        )

        expiredDrafts.forEach { handleExpiredDraft(it) }
    }

    private fun handleExpiredDraft(draft: MatchDraft) {
        logger.info("Processing expired draft: ${draft.id}")

        when (draft.currentState) {
            DraftState.CHARACTER_REVEAL -> handleExpiredCharacterReveal(draft)
            DraftState.DRAFTING -> handleExpiredDrafting(draft)
            else -> {}
        }

        if (isDraftCompleted(draft)) {
            draftCompletionService.completeDraft(draft)
        }
    }

    private fun isDraftCompleted(draft: MatchDraft): Boolean = draft.currentStepIndex >= draft.draftActions.size

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

        val action = if (draft.isCurrentStepPick()) {
            draftActionService.pickCharacter(draft, playerInfo, randomCharacter.name)
        } else {
            draftActionService.banCharacter(draft, playerInfo, randomCharacter.name)
        }

        logger.info(
            "Auto-${action.draftAction.actionType} character ${randomCharacter.name} for timeout in draft ${draft.id}",
        )

        applicationEventPublisher.publishEvent(
            AutomaticDraftActionPerformEvent(
                this,
                player = player,
                match = draft.match,
                action = action.draftAction,
            ),
        )
    }
}

package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.AutomaticDraftActionPerformEvent
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.service.match.MatchTimeoutService
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private const val DRAFT_CHECK_TIMEOUT_RATE_MS = 1000L

@Service
@Transactional
class DraftTimeoutService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftActionService: DraftActionService,
    private val matchTimeoutService: MatchTimeoutService,
    private val draftCompletionService: DraftCompletionService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    @Scheduled(fixedRate = DRAFT_CHECK_TIMEOUT_RATE_MS)
    fun checkTimeouts() {
        val expiredDrafts = matchDraftRepository.findByCurrentStateNot(DraftState.COMPLETED)
        expiredDrafts.forEach { handleExpiredDraft(it) }
    }

    private fun handleExpiredDraft(draft: MatchDraft) {
        logger.info("Processing expired draft: ${draft.id}")

        when (draft.currentState) {
            DraftState.CHARACTER_REVEAL -> handleExpiredPlayersReady(draft)
            DraftState.DRAFTING -> handleExpiredDrafting(draft)
            else -> {}
        }

        if (draft.isCompleted()) {
            draftCompletionService.completeDraft(draft)
        }
    }

    private fun handleExpiredPlayersReady(draft: MatchDraft) {
        if (draft.isPlayer1Ready && draft.isPlayer2Ready) {
            return
        }

        logger.warn(
            "Players not ready for draft: ${draft.id}, DEADLINE: ${draft.currentStateDeadline}, NOW: ${LocalDateTime.now()}",
        )

        if (draft.currentStateDeadline.isAfter(LocalDateTime.now())) {
            return
        }

        val player1Result = if (draft.isPlayer1Ready) LocalDateTime.MAX else draft.currentStateStartTime
        val player2Result = if (draft.isPlayer2Ready) LocalDateTime.MAX else draft.currentStateStartTime

        matchTimeoutService.endMatch(draft.match, MatchStatus.DRAW, null, "")

        // TODO: timeout log and ban after multiple player timeouts
        if (!draft.isPlayer1Ready) {
            with(player1Result) { TODO() }
        }
        if (!draft.isPlayer2Ready) {
            with(player2Result) { TODO() }
        }
    }

    private fun handleExpiredDrafting(draft: MatchDraft) {
        if (draft.currentStateDeadline.isAfter(LocalDateTime.now())) {
            return
        }

        val isPlayer1Turn = draft.isCurrentTurnPlayer1()
        val availablePool = if (isPlayer1Turn) {
            draft.player1Characters
        } else {
            draft.player2Characters
        }

        if (availablePool.isNotEmpty()) {
            performAutomaticDraftAction(draft, isPlayer1Turn, availablePool.map { it.name })
        } else {
            draft.moveToNextStep()
            matchDraftRepository.save(draft)
        }
    }

    private fun performAutomaticDraftAction(draft: MatchDraft, isPlayer1Turn: Boolean, availablePool: List<String>) {
        val randomCharacter = availablePool.random()
        val player = if (isPlayer1Turn) draft.match.player1 else draft.match.player2
        val playerInfo = PlayerInfo(player, isPlayer1Turn)

        val action = if (draft.isCurrentStepPick()) {
            draftActionService.pickCharacter(draft, playerInfo, randomCharacter)
        } else {
            draftActionService.banCharacter(draft, playerInfo, randomCharacter)
        }

        logger.info(
            "Auto-${if (action.draftAction.isPick) "pick" else "ban"} character $randomCharacter for timeout in draft ${draft.id}",
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

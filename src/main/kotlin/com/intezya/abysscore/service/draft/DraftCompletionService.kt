package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.repository.MatchDraftRepository
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DraftCompletionService(private val matchDraftRepository: MatchDraftRepository) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun completeDraft(draft: MatchDraft) {
        draft.currentState = DraftState.COMPLETED
        matchDraftRepository.save(draft)

        logger.info("Draft ${draft.id} completed")
        // TODO: notifyBothPlayers(draft, DraftNotificationType.DRAFT_COMPLETED)
    }
}

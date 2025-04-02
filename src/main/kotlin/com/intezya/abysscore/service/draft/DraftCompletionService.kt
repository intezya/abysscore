package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.DraftEndEvent
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DraftCompletionService(
    private val matchDraftRepository: MatchDraftRepository,
    private val matchRepository: MatchRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(this.javaClass)

    fun completeDraft(draft: MatchDraft) {
        draft.currentState = DraftState.COMPLETED
        matchDraftRepository.save(draft)

        val match = draft.match
        match.status = MatchStatus.ACTIVE
        matchRepository.save(match)

        logger.info("Draft ${draft.id} completed")
        applicationEventPublisher.publishEvent(DraftEndEvent(this, match = draft.match, draft = draft))
    }
}

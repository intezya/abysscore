package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.draft.DraftAction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DraftActionRepository : JpaRepository<DraftAction, Long> {
    fun findByDraftIdOrderByStepIndexAsc(draftId: Long): List<DraftAction>
}

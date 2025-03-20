package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.DraftAction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DraftActionRepository : JpaRepository<DraftAction, Long> {
    fun findByDraftIdOrderByTimestampDesc(draftId: Long): List<DraftAction>
}

package com.intezya.abysscore.repository

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.MatchDraft
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchDraftRepository : JpaRepository<MatchDraft, Long> {
    fun findByCurrentStateNot(state: DraftState): List<MatchDraft>
}

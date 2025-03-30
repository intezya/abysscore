package com.intezya.abysscore.repository

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.MatchDraft
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MatchDraftRepository : JpaRepository<MatchDraft, Long> {
    fun findByMatchId(matchId: Long): MatchDraft?
    fun findByCurrentStateNotAndCurrentStateDeadlineBefore(
        state: DraftState,
        deadline: LocalDateTime,
    ): List<MatchDraft>
}

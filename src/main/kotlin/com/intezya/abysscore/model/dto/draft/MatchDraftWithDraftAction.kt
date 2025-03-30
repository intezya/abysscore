package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.model.entity.draft.DraftAction
import com.intezya.abysscore.model.entity.draft.MatchDraft

data class MatchDraftWithDraftAction(
    val draft: MatchDraft,
    val draftAction: DraftAction,
)

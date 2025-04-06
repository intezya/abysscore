package com.intezya.abysscore.model.entity.draft

import com.intezya.abysscore.model.dto.draft.DraftStep
import okhttp3.internal.immutableListOf

class MatchDraftSchema {
    companion object {
        const val DRAFT_SCHEMA_SIZE = 22

        val DRAFT_SCHEMA = immutableListOf(
            // bbbb pppp pppp bb pppp pppp - 3 bans + 8 picks per player
            // 1212 1221 1221 21 2112 2112 - 11 actions per player
            DraftStep(firstPlayer = true, isPick = false),
            DraftStep(firstPlayer = false, isPick = false),
            DraftStep(firstPlayer = true, isPick = false),
            DraftStep(firstPlayer = false, isPick = false),

            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),

            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),

            DraftStep(firstPlayer = false, isPick = false),
            DraftStep(firstPlayer = true, isPick = false),

            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),

            DraftStep(firstPlayer = false, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = true, isPick = true),
            DraftStep(firstPlayer = false, isPick = true),
        )
    }
}

package com.intezya.abysscore.utils.fixtures

import com.intezya.abysscore.model.dto.draft.DraftStep
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import kotlin.random.Random

object MatchDraftFixtures {
    fun createDefaultMatchDraft(id: Long = 0L, match: Match = MatchFixtures.createDefaultMatch()): MatchDraft =
        MatchDraft().apply { this.id = id }

    fun createDefaultMatchDraft(id: Long = 0L): MatchDraft = MatchDraft().apply { this.id = id }

    fun createDefaultMatchDraft(
        id: Long = 0L,
        user1: User = UserFixtures.generateDefaultUser(),
        user2: User = UserFixtures.generateDefaultUser(),
    ): MatchDraft = MatchDraft().apply { this.id = id }

    fun provideRandomDraftSchema(): List<DraftStep> {
        val n = Random.nextInt(10, 100)
        val draft = { DraftStep(Random.nextBoolean(), Random.nextBoolean()) }
        return List(n) { draft() }
    }
}

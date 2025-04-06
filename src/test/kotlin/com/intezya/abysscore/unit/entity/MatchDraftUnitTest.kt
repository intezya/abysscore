package com.intezya.abysscore.unit.entity

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.utils.fixtures.MatchDraftFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MatchDraftUnitTest {
    @Nested
    inner class GetCurrentStepTest {
        @Test
        fun `should return null when draft not in DRAFTING state`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()

            draft.apply { this.currentState = DraftState.CHARACTER_REVEAL }

            val currentStep = draft.getCurrentStep()
            assertEquals(null, currentStep)
        }

        @Test
        fun `should return current step when draft in DRAFTING state`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()

            draft.apply { this.currentState = DraftState.DRAFTING }

            val currentStep = draft.getCurrentStep()
            assertEquals(draft.steps[0], currentStep)
        }
    }

    @Nested
    inner class MoveToNextStepTest {
        @Test
        fun `should move to next step`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()

            draft.apply { this.currentState = DraftState.DRAFTING }

            for (i in 0..draft.draftActions.size) {
                val currentStep = draft.getCurrentStep()
                assertEquals(draft.steps[i], currentStep)
                draft.moveToNextStep()
            }
        }
    }
}

package com.intezya.abysscore.unit.entity

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.model.entity.draft.DEFAULT_DRAFT_SCHEMA
import com.intezya.abysscore.utils.fixtures.MatchDraftFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class MatchDraftUnitTest {
    @Nested
    inner class DraftStepsSerializationTest {
        @Test
        fun `should serialize default draft steps`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()
            draft.setDraftSteps(DEFAULT_DRAFT_SCHEMA)

            val steps = draft.getDraftSteps()

            assertEquals(DEFAULT_DRAFT_SCHEMA, steps)
        }

        @RepeatedTest(value = 5)
        fun `should serialize any draft steps`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()
            val providedSteps = MatchDraftFixtures.provideRandomDraftSchema()
            draft.setDraftSteps(providedSteps)

            val steps = draft.getDraftSteps()

            assertEquals(providedSteps, steps)
        }
    }

    @Nested
    inner class GetCurrentStepTest {
        @Test
        fun `should return null when draft not in DRAFTING state`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()

            draft.apply { this.currentState = DraftState.DRAFTING }

            val currentStep = draft.getCurrentStep()
            assertEquals(null, currentStep)
        }

        @Test
        fun `should return current step when draft in DRAFTING state`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()
            val providedSteps = MatchDraftFixtures.provideRandomDraftSchema()

            draft.setDraftSteps(providedSteps)
            draft.apply { this.currentState = DraftState.DRAFTING }

            println(draft)
            println(draft.draftSchemaJson)
            println(draft.getDraftSteps())

            val currentStep = draft.getCurrentStep()
            assertEquals(draft.getDraftSteps()[0], currentStep)
        }
    }

    @Nested
    inner class MoveToNextStepTest {
        @Test
        fun `should move to next step`() {
            val draft = MatchDraftFixtures.createDefaultMatchDraft()
            val providedSteps = MatchDraftFixtures.provideRandomDraftSchema()

            draft.setDraftSteps(providedSteps)
            draft.apply { this.currentState = DraftState.DRAFTING }

            for (i in 0..draft.draftActions.size) {
                val currentStep = draft.getCurrentStep()
                assertEquals(draft.getDraftSteps()[i], currentStep)
                draft.moveToNextStep()
            }
        }
    }
}

package com.intezya.abysscore.controller.annotations

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireUserInMatch(
    val expected: Boolean,
    val matchStatus: MatchStatus = MatchStatus.UNSET,
    val draftState: DraftState = DraftState.UNSET,
)

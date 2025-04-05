package com.intezya.abysscore.controller

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.draft.MatchDraftDTO
import com.intezya.abysscore.model.dto.draft.PerformDraftActionRequest
import com.intezya.abysscore.model.dto.draft.toDTO
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.service.draft.DraftActionService
import com.intezya.abysscore.service.draft.DraftCharacterRevealService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/matches/current/draft/process")
class DraftProcessController(
    private val draftCharacterRevealService: DraftCharacterRevealService,
    private val draftActionService: DraftActionService,
) {
    @PostMapping("/characters")
    @RequireUserInMatch(
        expected = true,
        matchStatus = MatchStatus.PENDING,
        draftState = DraftState.CHARACTER_REVEAL,
    )
    fun revealCharacters(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid characters: List<DraftCharacterDTO>,
    ): MatchDraftDTO = draftCharacterRevealService.revealCharacters(user, characters).toDTO()

    @PostMapping("")
    @RequireUserInMatch(
        expected = true,
        matchStatus = MatchStatus.DRAFTING,
        draftState = DraftState.DRAFTING,
    )
    fun performDraftAction(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: PerformDraftActionRequest,
    ): MatchDraftDTO = draftActionService.performDraftAction(user, request.characterName).toDTO()
}

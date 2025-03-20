package com.intezya.abysscore.controller

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.draft.MatchDraftDTO
import com.intezya.abysscore.model.dto.draft.PerformDraftActionRequest
import com.intezya.abysscore.model.dto.draft.toDTO
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.service.DraftProcessService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/matches/current/draft/process")
class DraftProcessController(
    private val draftProcessService: DraftProcessService,
) {
    @PostMapping("/characters")
    @RequireUserInMatch(expectedThat = true)
    fun revealCharacters(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid characters: List<DraftCharacterDTO>,
    ): MatchDraftDTO = draftProcessService.revealCharacters(user, characters).toDTO()

    @PostMapping("")
    @RequireUserInMatch(expectedThat = true)
    fun performDraftAction(
        @AuthenticationPrincipal user: User,
        @RequestBody @Valid request: PerformDraftActionRequest,
    ): MatchDraftDTO = draftProcessService.performDraftAction(user, request.characterName).toDTO()
}

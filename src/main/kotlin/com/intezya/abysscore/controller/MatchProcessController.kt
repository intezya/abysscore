package com.intezya.abysscore.controller

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.model.dto.match.MatchDTO
import com.intezya.abysscore.model.dto.match.toDTO
import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.service.MatchProcessService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/matches/current/process")
class MatchProcessController(private val matchProcessService: MatchProcessService) {
    @PostMapping("/submit-retry")
    @RequireUserInMatch(expectedThat = true)
    fun submitRetry(
        @RequestBody @Valid request: SubmitRoomResultRequest,
        @AuthenticationPrincipal contextUser: User,
    ): MatchDTO = matchProcessService.submitRetry(contextUser, request).toDTO()

    @PostMapping("/submit-result")
    @RequireUserInMatch(expectedThat = true)
    fun submitResult(
        @RequestBody @Valid request: SubmitRoomResultRequest,
        @AuthenticationPrincipal contextUser: User,
    ): MatchDTO = matchProcessService.submitResult(contextUser, request).toDTO()
}

package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.matchinvite.MatchInviteDTO
import com.intezya.abysscore.model.dto.matchinvite.toDTO
import com.intezya.abysscore.security.dto.AuthDTO
import com.intezya.abysscore.service.MatchInviteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/match/invites")
class MatchInviteController(
    private val matchInviteService: MatchInviteService,
) {
    @PostMapping("")
    fun inviteUser(
        @RequestBody @Valid inviteRequest: CreateMatchInviteRequest,
        @AuthenticationPrincipal userDetails: AuthDTO,
    ): ResponseEntity<MatchInviteDTO> = ResponseEntity(
        matchInviteService.create(userDetails.id, inviteRequest.inviteeId).toDTO(),
        HttpStatus.CREATED,
    )

    @PostMapping("{inviteId}/accept")
    @ResponseStatus(HttpStatus.OK)
    fun acceptInvite(
        @PathVariable inviteId: Long,
        @AuthenticationPrincipal userDetails: AuthDTO,
    ) = matchInviteService.acceptInvite(userDetails.id, inviteId)

    @PostMapping("{inviteId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun declineInvite(
        @PathVariable inviteId: Long,
        @AuthenticationPrincipal userDetails: AuthDTO,
    ) = matchInviteService.declineInvite(userDetails.id, inviteId)
}

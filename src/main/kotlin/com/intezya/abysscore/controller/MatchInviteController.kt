package com.intezya.abysscore.controller

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.model.dto.match.CurrentMatchDTO
import com.intezya.abysscore.model.dto.match.toCurrentMatchDTO
import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.matchinvite.MatchInviteDTO
import com.intezya.abysscore.model.dto.matchinvite.toDTO
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.service.MatchInviteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/invites")
class MatchInviteController(private val matchInviteService: MatchInviteService) {
    @PostMapping("")
    @RequireUserInMatch(expected = false)
    fun inviteUser(
        @RequestBody @Valid inviteRequest: CreateMatchInviteRequest,
        @AuthenticationPrincipal contextUser: User,
    ): ResponseEntity<MatchInviteDTO> = ResponseEntity(
        matchInviteService.create(contextUser.id, inviteRequest.inviteeUsername).toDTO(),
        HttpStatus.CREATED,
    )

    @PostMapping("{inviteId}/accept")
    @RequireUserInMatch(expected = false)
    @ResponseStatus(HttpStatus.OK)
    fun acceptInvite(@PathVariable inviteId: Long, @AuthenticationPrincipal contextUser: User): CurrentMatchDTO =
        matchInviteService.acceptInvite(contextUser.id, inviteId).toCurrentMatchDTO()

    @PostMapping("{inviteId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun declineInvite(@PathVariable inviteId: Long, @AuthenticationPrincipal contextUser: User) =
        matchInviteService.declineInvite(contextUser.id, inviteId)
}

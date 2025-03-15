package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.security.dto.AuthDTO
import com.intezya.abysscore.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal userDetails: AuthDTO,
    ): ResponseEntity<UserDTO> = ResponseEntity.ok(userService.findUserWithThrow(userDetails.id).toDTO())

    @GetMapping("")
    @RequiresAccessLevel(AccessLevel.VIEW_ALL_USERS)
    fun getAll(
        @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<UserDTO> = PagedModel(userService.findAll(pageable))

    @PatchMapping("/preferences/invites")
    fun updateReceiveMatchInvites(
        @RequestBody @Valid receiveMatchInvites: UpdateMatchInvitesRequest,
        @AuthenticationPrincipal userDetails: AuthDTO,
    ): ResponseEntity<UserDTO> = ResponseEntity.ok(
        userService.updateReceiveMatchInvites(userDetails.id, receiveMatchInvites),
    )
}

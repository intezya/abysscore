package com.intezya.abysscore.controller.crud

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import com.intezya.abysscore.model.dto.user.UpdateProfileBadgeRequest
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.service.UserService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
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
        @AuthenticationPrincipal contextUser: User,
    ): ResponseEntity<UserDTO> = ResponseEntity.ok(userService.findUserWithThrow(contextUser.id).toDTO())

    @GetMapping("")
    @RequiresAccessLevel(AccessLevel.VIEW_ALL_USERS)
    fun getAll(
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<UserDTO> = PagedModel(userService.findAll(pageable))

    @PatchMapping("/preferences/invites")
    fun updateReceiveMatchInvites(
        @RequestBody @Valid request: UpdateMatchInvitesRequest,
        @AuthenticationPrincipal contextUser: User,
    ): ResponseEntity<UserDTO> = ResponseEntity.ok(
        userService.updateReceiveMatchInvites(contextUser.id, request),
    )

    @PatchMapping("/preferences/badge")
    fun updateBadge(
        @RequestBody @Valid request: UpdateProfileBadgeRequest,
        @AuthenticationPrincipal contextUser: User,
    ): ResponseEntity<UserDTO> = ResponseEntity.ok(
        userService.updateBadge(contextUser.id, request),
    )
}

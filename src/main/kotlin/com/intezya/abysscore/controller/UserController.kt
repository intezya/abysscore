package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.security.service.AuthDTO
import com.intezya.abysscore.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(
        authentication: Authentication,
    ): ResponseEntity<UserDTO> {
        println(authentication)
        println(authentication.principal as AuthDTO)
        return ResponseEntity.ok(userService.me(authentication.name))
    }

    @GetMapping("")
    @RequiresAccessLevel(AccessLevel.VIEW_ALL_USERS)
    fun getAll(
        @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<UserDTO> = PagedModel(userService.findAll(pageable))
}

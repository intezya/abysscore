package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.security.service.AuthDTO
import com.intezya.abysscore.service.UserService
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
}

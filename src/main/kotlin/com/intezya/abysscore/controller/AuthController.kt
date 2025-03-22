package com.intezya.abysscore.controller

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.dto.AuthResponse
import com.intezya.abysscore.security.utils.CustomAuthenticationToken
import com.intezya.abysscore.security.utils.JwtUtils
import com.intezya.abysscore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val userService: UserService,
) {
    @PostMapping("/register")
    fun registerUser(
        @RequestBody @Valid request: AuthRequest,
//        httpRequest: HttpServletRequest,
    ): ResponseEntity<AuthResponse> {
//        userService.create(authRequest, jwtUtils.getClientIp(httpRequest))
        val user = userService.create(request)
        val token = jwtUtils.generateToken(user)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val authentication = authenticationManager.authenticate(
            CustomAuthenticationToken(
                request.username,
                request.password,
                request.hwid,
            ),
        )
        val token = jwtUtils.generateToken(authentication.principal as User)
        return ResponseEntity.ok(AuthResponse(token))
    }
}

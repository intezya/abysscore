package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.dto.user.UserAuthResponse
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import com.intezya.abysscore.security.jwt.JwtUtils
import com.intezya.abysscore.security.service.AuthenticationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val jwtUtils: JwtUtils,
) {
    @PostMapping("/register")
    fun registerUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse = authenticationService.registerUser(userAuthRequest, jwtUtils.getClientIp(request))

    @PostMapping("/login")
    fun loginUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse = authenticationService.loginUser(userAuthRequest, jwtUtils.getClientIp(request))

    @GetMapping("/info")
    fun getUserInfo(
        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
    ): UserAuthInfoDTO = userAuthData
}

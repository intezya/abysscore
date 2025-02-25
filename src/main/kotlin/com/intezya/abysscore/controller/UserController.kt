package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.dto.user.UserAuthRequest
import com.intezya.abysscore.dto.user.UserAuthResponse
import com.intezya.abysscore.service.UserService
import com.intezya.abysscore.utils.AuthUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account")
class UserController(
    private val userService: UserService,
    private val authUtils: AuthUtils,
) {
    @PostMapping("/register")
    fun registerUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse {
        return userService.registerUser(userAuthRequest, authUtils.getClientIp(request))
    }


    @PostMapping("/login")
    fun loginUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse {
        return userService.loginUser(userAuthRequest, authUtils.getClientIp(request))
    }

    @GetMapping("/info")
    fun getUserInfo(): UserAuthInfoDTO {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as UserAuthInfoDTO
    }
}

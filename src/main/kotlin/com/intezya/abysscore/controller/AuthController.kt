package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.admin.AdminAuthRequest
import com.intezya.abysscore.model.dto.admin.AdminAuthResponse
import com.intezya.abysscore.model.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.dto.user.UserAuthResponse
import com.intezya.abysscore.service.AuthenticationService
import com.intezya.abysscore.utils.auth.AuthUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val authUtils: AuthUtils,
) {
    @PostMapping("/register")
    fun registerUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse {
        return authenticationService.registerUser(userAuthRequest, authUtils.getClientIp(request))
    }

    @PostMapping("/login")
    fun loginUser(
        @RequestBody @Valid userAuthRequest: UserAuthRequest,
        request: HttpServletRequest,
    ): UserAuthResponse {
        return authenticationService.loginUser(userAuthRequest, authUtils.getClientIp(request))
    }

    @PostMapping("/admin/login")
    fun loginAdmin(
        @RequestBody @Valid adminAuthRequest: AdminAuthRequest,
        request: HttpServletRequest,
    ): AdminAuthResponse {
        return authenticationService.adminLogin(adminAuthRequest, authUtils.getClientIp(request))
    }

    @GetMapping("/info")
    fun getUserInfo(
        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
    ): UserAuthInfoDTO {
        return userAuthData
    }
}

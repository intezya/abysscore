package com.intezya.abysscore.service

import com.intezya.abysscore.dto.admin.AdminAuthRequest
import com.intezya.abysscore.dto.admin.AdminAuthResponse
import com.intezya.abysscore.dto.user.UserAuthRequest
import com.intezya.abysscore.dto.user.UserAuthResponse
import com.intezya.abysscore.entity.User
import com.intezya.abysscore.repository.AdminRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.utils.AuthUtils
import com.intezya.abysscore.utils.PasswordUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordUtils: PasswordUtils,
    private val authUtils: AuthUtils,
    private val adminRepository: AdminRepository,
) {
    fun register(request: UserAuthRequest): UserAuthResponse {
        return try {
            val user = User(
                username = request.username,
                password = passwordUtils.hashPassword(request.password),
                hwid = passwordUtils.hashHwid(request.hwid),
            )
            userRepository.save(user)
            UserAuthResponse(token = authUtils.generateJwtToken(user))
        } catch (ex: Exception) {
            if (ex.message?.contains("uc_users_username") == true) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
            }
            if (ex.message?.contains("uc_users_hwid") == true) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Only 1 account allowed per device")
            }
            throw ex
        }
    }

    fun authenticate(request: UserAuthRequest): UserAuthResponse {
        val user = userRepository.findByUsername(request.username).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }

        if (!passwordUtils.verifyPassword(request.password, user.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password")
        }

        if (user.hwid != null && !passwordUtils.verifyHwid(request.hwid, user.hwid)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid hardware ID")
        }

        return UserAuthResponse(token = authUtils.generateJwtToken(user))
    }

    fun authenticateAdmin(request: AdminAuthRequest): AdminAuthResponse {
        authenticate(request.toUserAuthRequest())
        val user = userRepository.findByUsername(request.username).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        val admin = adminRepository.findByUserId(user.id!!).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        return AdminAuthResponse(
            token = authUtils.generateJwtToken(
                user,
                accessLevel = admin.accessLevel.value,
                extraExpirationMs = 3600000, // 1h
            )
        )
    }
}

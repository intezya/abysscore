package com.intezya.abysscore.service

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordUtils: PasswordUtils,
) {
    fun create(request: AuthRequest): User {
        if (userRepository.findByUsername(request.username).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
        }
        val user = User(
            username = request.username,
            password = passwordUtils.hashPassword(request.password),
            hwid = passwordUtils.hashHwid(request.hwid),
        )
        try {
            return userRepository.save(user)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only 1 account allowed per device")
        }
    }

    fun findUserWithThrow(userId: Long): User = userRepository.findById(userId).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    fun findUserWithThrow(username: String): User = userRepository.findByUsername(username).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
}

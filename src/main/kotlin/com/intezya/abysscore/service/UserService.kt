package com.intezya.abysscore.service

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findUserWithThrow(userId: Long): User = userRepository.findById(userId).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    fun findUserWithThrow(username: String): User = userRepository.findByUsername(username).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
}

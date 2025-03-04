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
    fun findUserWithThrow(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID $userId does not exist")
        }
    }

    fun findUserWithThrow(username: String): User {
        return userRepository.findByUsername(username).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with username $username does not exist")
        }
    }
}

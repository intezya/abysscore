package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserGlobalStatistic
import com.intezya.abysscore.repository.UserGlobalStatisticRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userGlobalStatisticRepository: UserGlobalStatisticRepository,
    private val passwordUtils: PasswordUtils,
) {
    fun create(request: AuthRequest): User {
        val user = User(
            username = request.username,
            password = passwordUtils.hashPassword(request.password),
            hwid = passwordUtils.hashHwid(request.hwid),
        )

        try {
            userRepository.save(user)
        } catch (e: Exception) {
            handleUserCreationError(e)
        }

        createGlobalStatisticOnRegister(user)

        return user
    }

    fun findUserWithThrow(userId: Long): User = userRepository.findById(userId).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    fun findUserWithThrow(username: String): User = userRepository.findByUsername(username).orElseThrow {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }

    fun me(username: String): UserDTO = findUserWithThrow(username).toDTO()

    fun findAll(pageable: Pageable): Page<UserDTO> = userRepository.findAll(pageable).map { it.toDTO() }

    private fun handleUserCreationError(e: Exception): Nothing {
        when {
            e.message?.contains("uc_users_username") == true ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")

            e.message?.contains("uc_users_hwid") == true ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "User already has account on device")

            else -> throw IllegalStateException("Failed to create user", e)
        }
    }

    private fun createGlobalStatisticOnRegister(user: User) {
        userGlobalStatisticRepository.save(
            UserGlobalStatistic(
                user = user,
            ),
        )
    }
}

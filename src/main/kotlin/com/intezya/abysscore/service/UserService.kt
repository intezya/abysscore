package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import com.intezya.abysscore.model.dto.user.UpdateProfileBadgeRequest
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.dto.user.toDTO
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserGlobalStatistic
import com.intezya.abysscore.model.entity.UserItem
import com.intezya.abysscore.repository.UserGlobalStatisticRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
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
            createGlobalStatisticForUser(user)
            return user
        } catch (e: Exception) {
            handleUserCreationError(e)
        }
    }

    @Transactional(readOnly = true)
    fun findUserWithThrow(userId: Long): User = userRepository.findById(userId)
        .orElseThrow { createUserNotFoundException() }

    @Transactional(readOnly = true)
    fun findUserWithThrow(username: String): User = userRepository.findByUsername(username)
        .orElseThrow { createUserNotFoundException() }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<UserDTO> = userRepository.findAll(pageable).map { it.toDTO() }

    fun updateReceiveMatchInvites(userId: Long, request: UpdateMatchInvitesRequest): UserDTO {
        val user = findUserWithThrow(userId)
        user.receiveMatchInvites = request.receiveMatchInvites
        return userRepository.save(user).toDTO()
    }

    fun updateBadge(userId: Long, request: UpdateProfileBadgeRequest): UserDTO {
        val user = findUserWithThrow(userId)
        val badge = user.badgeInInventory(request.itemId) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User does not own badge",
        )

        user.currentBadge = badge
        return userRepository.save(user).toDTO()
    }

    fun setCurrentMatch(user: User, match: Match): User {
        user.currentMatch = match
        return userRepository.save(user)
    }

    private fun createUserNotFoundException() = ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    private fun handleUserCreationError(e: Exception): Nothing {
        when {
            e.message?.contains("uc_users_username") == true ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")

            e.message?.contains("uc_users_hwid") == true ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "User already has account on device")

            else -> throw IllegalStateException("Failed to create user", e)
        }
    }

    private fun createGlobalStatisticForUser(user: User) {
        val statistic = UserGlobalStatistic().apply { this.user = user }
        userGlobalStatisticRepository.save(statistic)
    }

    private fun User.badgeInInventory(badgeId: Long): UserItem? = items.firstOrNull { it.gameItem.id == badgeId }
}

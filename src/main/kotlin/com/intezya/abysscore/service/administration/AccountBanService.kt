package com.intezya.abysscore.service.administration

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.event.user.account.AccountBannedEvent
import com.intezya.abysscore.model.entity.user.BanHistory
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.BanHistoryRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.service.UserService
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
class AccountBanService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val banHistoryRepository: BanHistoryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = LogFactory.getLog(AccountBanService::class.java)

    fun banUser(userId: Long, adminId: Long, banUntil: LocalDateTime, reason: String? = null) {
        val admin = userService.findUserWithThrow(adminId)
        if (admin.accessLevel < AccessLevel.USER_BAN) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permissions to ban users",
            )
        }

        val bannedBy = admin
        applyBan(userId, banUntil, reason, bannedBy)
        logger.info("User ID $userId banned by admin ID $adminId until $banUntil")
    }

    fun banUserBySystem(userId: Long, banUntil: LocalDateTime, reason: String? = null) {
        applyBan(userId, banUntil, reason, null)
        logger.info("User ID $userId banned by system until $banUntil")
    }

    private fun applyBan(userId: Long, banUntil: LocalDateTime, reason: String?, bannedBy: User?) {
        validateBanDuration(banUntil)

        val user = userService.findUserWithThrow(userId)
        user.bannedUntil = banUntil
        user.banReason = reason

        val banLog = BanHistory(
            user = user,
            bannedBy = bannedBy,
            expiresAt = banUntil,
            reason = reason,
        )

        banHistoryRepository.save(banLog)
        userRepository.save(user)
        eventPublisher.publishEvent(AccountBannedEvent(this, user))
    }

    private fun validateBanDuration(banUntil: LocalDateTime) {
        if (banUntil.isBefore(LocalDateTime.now())) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ban expiration time must be in the future")
        }
    }

    fun unbanUser(userId: Long, adminId: Long, reason: String? = null) {
        val admin = userService.findUserWithThrow(adminId)
        if (admin.accessLevel < AccessLevel.USER_BAN) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permissions to unban users",
            )
        }

        val user = userService.findUserWithThrow(userId)
        if (user.bannedUntil?.isBefore(LocalDateTime.now()) == true) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User is not currently banned",
            )
        }

        user.bannedUntil = null
        user.banReason = null

        val activeBans = banHistoryRepository.findActiveBansForUser(userId)
        activeBans.forEach { it.approveDispute(admin, reason) }

        banHistoryRepository.saveAll(activeBans)
        userRepository.save(user)

        logger.info("User ID $userId unbanned by admin ID $adminId")
    }
}

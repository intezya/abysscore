package com.intezya.abysscore.service.administration

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.event.user.account.AccountBannedEvent
import com.intezya.abysscore.model.entity.user.BanHistory
import com.intezya.abysscore.repository.BanHistoryRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.service.UserService
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional
class AccountBanService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val banHistoryRepository: BanHistoryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun banUser(userId: Long, adminId: Long, banUntil: LocalDateTime, reason: String? = null) {
        val user = userService.findUserWithThrow(userId)
        val admin = userService.findUserWithThrow(adminId)

        // Double check if user has rights
        if (admin.accessLevel < AccessLevel.USER_BAN) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permissions to ban users",
            )
        }

        user.bannedUntil = banUntil
        user.banReason = reason
        val banLog = BanHistory(
            user = user,
            bannedBy = admin,
            expiresAt = banUntil,
            reason = reason,
        )

        banHistoryRepository.save(banLog)
        userRepository.save(user)

        eventPublisher.publishEvent(AccountBannedEvent(this, user))
    }
}

package com.intezya.abysscore.service

import com.intezya.abysscore.dto.event.ItemIssueEvent
import com.intezya.abysscore.dto.user_item.UserItemDTO
import com.intezya.abysscore.entity.Admin
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.entity.User
import com.intezya.abysscore.entity.UserItem
import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.repository.AdminRepository
import com.intezya.abysscore.repository.UserItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserItemService(
    private val userService: UserService,
    private val adminRepository: AdminRepository,
    private val gameItemService: GameItemService,
    private val userItemRepository: UserItemRepository,
    private val eventPublisher: EventPublisher,
) {
    companion object {
        private const val ISSUED_BY_SYSTEM = 0L
        private const val ITEM_ISSUE_EVENT_TOPIC = "item-issue-events"
    }

    fun findAllUserItems(userId: Long, pageable: Pageable): Page<UserItemDTO> {
        userService.findUserWithThrow(userId)
        val userItemsPage = userItemRepository.findByUserId(userId, pageable)
        return userItemsPage.map { it.toDTO() }
    }

    @Transactional
    fun issueForPlayerFromAdmin(username: String, itemId: Long, adminId: Long): UserItemDTO {
        val user = userService.findUserWithThrow(username)
        val gameItem = gameItemService.findById(itemId)
        val admin = adminRepository.findById(adminId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Admin with id $adminId not found")
        }
        return issueByAdmin(user, gameItem, admin).toDTO()
    }

    private fun issueBySystem(user: User, item: GameItem): UserItem {
        val userItem = UserItem(
            user = user,
            gameItem = item,
            sourceType = ItemSourceType.SYSTEM,
        )
        sendEvent(user.id!!, item.id!!, ISSUED_BY_SYSTEM)
        return userItemRepository.save(userItem)
    }

    private fun issueByAdmin(user: User, item: GameItem, admin: Admin): UserItem {
        val userItem = UserItem(
            user = user,
            gameItem = item,
            sourceType = ItemSourceType.ADMIN,
        )
        sendEvent(user.id!!, item.id!!, admin.id)
        return userItemRepository.save(userItem)
    }

    private fun sendEvent(receiverId: Long, itemId: Long, issuedBy: Long) {
        val event = ItemIssueEvent(
            itemId = itemId,
            receiverId = receiverId,
            issuedBy = issuedBy,
        )
        eventPublisher.sendActionEvent(event, event.receiverId.toString(), ITEM_ISSUE_EVENT_TOPIC)
    }
}

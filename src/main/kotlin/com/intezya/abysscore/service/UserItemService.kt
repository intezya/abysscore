package com.intezya.abysscore.service

import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.model.dto.event.ItemIssueEvent
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.model.dto.useritem.toDTO
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import com.intezya.abysscore.repository.UserItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserItemService(
    private val userService: UserService,
    private val gameItemService: GameItemService,
    private val userItemRepository: UserItemRepository,
    private val eventPublisher: EventPublisher,
) {
    companion object {
        private const val ISSUED_BY_SYSTEM = 0L
        private const val ITEM_ISSUE_EVENT_TOPIC = "item-issue-events"
    }

    fun findAllUserItems(
        userId: Long,
        pageable: Pageable,
    ): Page<UserItemDTO> {
        userService.findUserWithThrow(userId)
        val userItemsPage = userItemRepository.findByUserId(userId, pageable)
        return userItemsPage.map { it.toDTO() }
    }

    @Transactional
    fun issueForPlayerFromAdmin(
        userId: Long,
        itemId: Long,
        adminId: Long,
    ): UserItemDTO {
        val user = userService.findUserWithThrow(userId)
        val gameItem = gameItemService.findById(itemId)
        val admin = userService.findUserWithThrow(adminId)
        return issueByAdmin(user, gameItem, admin).toDTO()
    }

    private fun issueBySystem(
        user: User,
        item: GameItem,
    ): UserItem {
        val userItem = UserItem(sourceType = ItemSourceType.SYSTEM).apply {
            this.user = user
            this.gameItem = item
        }
        sendEvent(user.id, item.id, ISSUED_BY_SYSTEM)
        return userItemRepository.save(userItem)
    }

    private fun issueByAdmin(
        user: User,
        item: GameItem,
        admin: User,
    ): UserItem {
        val userItem =
            UserItem(sourceType = ItemSourceType.ADMIN).apply {
                this.user = user
                this.gameItem = item
            }
        sendEvent(user.id, item.id, admin.id)
        return userItemRepository.save(userItem)
    }

    private fun sendEvent(
        receiverId: Long,
        itemId: Long,
        issuedBy: Long,
    ) {
        val event =
            ItemIssueEvent(
                itemId = itemId,
                receiverId = receiverId,
                issuedBy = issuedBy,
            )
        eventPublisher.sendActionEvent(event, event.receiverId.toString(), ITEM_ISSUE_EVENT_TOPIC)
    }
}

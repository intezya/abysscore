package com.intezya.abysscore.service

import com.intezya.abysscore.dto.event.ItemIssueEvent
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.entity.User
import com.intezya.abysscore.entity.UserItem
import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.repository.UserItemRepository

class UserItemService(
    private val userItemRepository: UserItemRepository,
    private val eventPublisher: EventPublisher,
) {
    companion object {
        private const val ISSUED_BY_SYSTEM = 0L
        private const val ITEM_ISSUE_EVENT_TOPIC = "item-issue-events"
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

    private fun issueByAdmin(user: User, item: GameItem, adminId: Long): UserItem {
        val userItem = UserItem(
            user = user,
            gameItem = item,
            sourceType = ItemSourceType.SYSTEM,
        )
        sendEvent(user.id!!, item.id!!, adminId)
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

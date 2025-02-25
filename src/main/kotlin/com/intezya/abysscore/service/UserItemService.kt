package com.intezya.abysscore.service

import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.entity.User
import com.intezya.abysscore.entity.UserItem
import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.repository.UserItemRepository

class UserItemService(
    private val userItemRepository: UserItemRepository,
) {
    private fun issueBySystem(user: User, item: GameItem): UserItem {
        val userItem = UserItem(
            user = user,
            gameItem = item,
            sourceType = ItemSourceType.SYSTEM,
        )
        return userItemRepository.save(userItem)
    }
}

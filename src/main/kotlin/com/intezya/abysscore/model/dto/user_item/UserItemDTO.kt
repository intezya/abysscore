package com.intezya.abysscore.model.dto.user_item

import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.enum.ItemSourceType
import java.time.LocalDateTime

data class UserItemDTO(
    var id: Long,
    val gameItem: GameItem,
    val receivedFrom: Long? = null,
    val sourceType: ItemSourceType,
    val createdAt: LocalDateTime,
)

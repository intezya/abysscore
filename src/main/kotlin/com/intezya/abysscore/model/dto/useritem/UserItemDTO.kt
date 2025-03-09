package com.intezya.abysscore.model.dto.useritem

import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.model.entity.GameItem
import java.time.LocalDateTime

data class UserItemDTO(
    var id: Long,
    val gameItem: GameItem,
    val receivedFrom: Long? = null,
    val sourceType: ItemSourceType,
    val createdAt: LocalDateTime,
)

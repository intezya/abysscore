package com.intezya.abysscore.model.dto.useritem

import com.intezya.abysscore.model.dto.gameitem.GameItemDTO
import com.intezya.abysscore.model.entity.UserItem
import java.time.LocalDateTime

data class UserItemDTO(
    var id: Long,
    val gameItem: GameItemDTO,
//    val receivedFrom: Long? = null,
//    val sourceType: ItemSourceType,
    val createdAt: LocalDateTime,
) {
    constructor(userItem: UserItem) : this(
        id = userItem.id,
        gameItem = GameItemDTO(userItem.gameItem!!),
//        receivedFrom = userItem.receivedFrom?.id,
//        sourceType = userItem.sourceType,
        createdAt = userItem.createdAt,
    )
}

fun UserItem.toDTO(): UserItemDTO = UserItemDTO(this)

package com.intezya.abysscore.model.dto.useritem

import com.intezya.abysscore.model.entity.item.UserItem

data class UserItemViewDTO(
    var id: Long,
    val name: String,
    val collection: String,
    val type: Int,
    val rarity: Int,
) {
    constructor(userItem: UserItemDTO) : this(
        id = userItem.id,
        name = userItem.gameItem.name,
        collection = userItem.gameItem.collection,
        type = userItem.gameItem.type,
        rarity = userItem.gameItem.rarity,
    )
}

fun UserItemDTO.toViewDTO(): UserItemViewDTO = UserItemViewDTO(this)
fun UserItem.toViewDTO(): UserItemViewDTO = UserItemViewDTO(this.toDTO())

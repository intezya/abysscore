package com.intezya.abysscore.model.dto.gameitem

import com.intezya.abysscore.model.entity.item.GameItem

data class GameItemDTO(var id: Long, val name: String, val collection: String, val type: Int, val rarity: Int) {
    constructor(gameItem: GameItem) : this(
        id = gameItem.id,
        name = gameItem.name,
        collection = gameItem.collection,
        type = gameItem.type,
        rarity = gameItem.rarity,
    )
}

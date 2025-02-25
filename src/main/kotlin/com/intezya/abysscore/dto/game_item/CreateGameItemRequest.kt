package com.intezya.abysscore.dto.game_item

import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.enum.GameItemRarity
import com.intezya.abysscore.enum.GameItemType
import com.intezya.abysscore.utils.validator.ValidGameItemRarity
import com.intezya.abysscore.utils.validator.ValidGameItemType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateGameItemRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val collection: String,

    @field:NotNull
    @field:ValidGameItemType
    val type: GameItemType,
    @field:NotNull
    @field:ValidGameItemRarity
    val rarity: GameItemRarity,
) {
    fun toEntity(): GameItem {
        return GameItem(null, name, collection, type, rarity)
    }
}

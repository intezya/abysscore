package com.intezya.abysscore.model.dto.gameitem

import com.intezya.abysscore.model.entity.GameItem
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateGameItemRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val collection: String,
    @field:NotNull
    @field:Min(0)
    val type: Int,
    @field:NotNull
    @field:Min(0)
    val rarity: Int,
) {
    fun toEntity(): GameItem = GameItem(null, name, collection, type, rarity)
}

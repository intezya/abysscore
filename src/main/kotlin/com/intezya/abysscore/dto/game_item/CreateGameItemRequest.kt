package com.intezya.abysscore.dto.game_item

import com.fasterxml.jackson.annotation.JsonCreator
import com.intezya.abysscore.entity.GameItem
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateGameItemRequest @JsonCreator constructor(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val collection: String,

    @field:NotNull
    @field:Min(0)
    @field:Max(2)
    val type: Int,
    @field:NotNull
    @field:Min(0)
    @field:Max(5)
    val rarity: Int,
) {
    fun toEntity(): GameItem {
        return GameItem(null, name, collection, type, rarity)
    }
}

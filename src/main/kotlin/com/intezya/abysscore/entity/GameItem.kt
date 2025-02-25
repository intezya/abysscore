package com.intezya.abysscore.entity

import com.intezya.abysscore.enum.GameItemRarity
import com.intezya.abysscore.enum.GameItemType
import jakarta.persistence.*

@Entity
@Table(name = "game_items")
data class GameItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val name: String,
    val collection: String,
    val type: GameItemType,
    val rarity: GameItemRarity
) {
    constructor() : this(null, "", "", GameItemType.BADGE, GameItemRarity.COMMON)
}

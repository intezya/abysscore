package com.intezya.abysscore.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "game_items")
data class GameItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    val name: String,
    val collection: String,
    val type: Int,
    val rarity: Int,
) {
    constructor() : this(
        id = 0L,
        name = "",
        collection = "",
        type = 0,
        rarity = 0,
    )
}

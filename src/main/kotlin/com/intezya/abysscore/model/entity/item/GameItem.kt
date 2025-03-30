package com.intezya.abysscore.model.entity.item

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "game_items")
class GameItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    val name: String

    val collection: String

    val type: Int

    val rarity: Int

    constructor() : this(
        name = "",
        collection = "",
        type = 0,
        rarity = 0,
    )

    constructor(
        id: Long = 0L,
        name: String,
        collection: String,
        type: Int,
        rarity: Int,
    ) {
        this.id = id
        this.name = name
        this.collection = collection
        this.type = type
        this.rarity = rarity
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameItem) return false
        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            name == other.name &&
                collection == other.collection &&
                type == other.type &&
                rarity == other.rarity
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(name, collection, type, rarity)
    }

    override fun toString(): String = this::class.simpleName +
        "(id = $id , name = $name , collection = $collection , type = $type , rarity = $rarity )"
}

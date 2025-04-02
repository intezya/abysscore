package com.intezya.abysscore.model.entity.draft

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "draft_characters")
class DraftCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long

    @Column(nullable = false)
    val name: String

    @Column(nullable = false)
    val element: String

    @Column(nullable = false)
    val level: Int

    @Column(nullable = false)
    val rarity: Int

    @Column(nullable = false)
    val constellations: Int

    constructor() : this(
        id = 0L,
        name = "",
        element = "",
        level = 0,
        rarity = 0,
        constellations = 0,
    )

    constructor(
        id: Long = 0L,
        name: String,
        element: String,
        level: Int,
        rarity: Int,
        constellations: Int,
    ) {
        this.id = id
        this.name = name
        this.element = element
        this.level = level
        this.rarity = rarity
        this.constellations = constellations
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DraftCharacter) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            name == other.name &&
                element == other.element &&
                level == other.level &&
                rarity == other.rarity &&
                constellations == other.constellations
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(name, element, level, rarity, constellations)
    }

    override fun toString(): String =
        "DraftCharacter(id=$id, name='$name', element='$element', level=$level, rarity=$rarity, constellations=$constellations)"
}

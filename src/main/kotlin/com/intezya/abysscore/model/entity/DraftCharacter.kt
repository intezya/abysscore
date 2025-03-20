package com.intezya.abysscore.model.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "draft_characters",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name", "constellations", "element", "level", "rarity"]),
    ],
)
data class DraftCharacter(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val element: String = "",

    @Column(nullable = false)
    val level: Int = 0,

    @Column(nullable = false)
    val rarity: Int = 0,

    @Column(nullable = false)
    val constellations: Int = 0,
)

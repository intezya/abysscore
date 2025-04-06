package com.intezya.abysscore.model.entity.draft

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class CharacterData(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val element: String,

    @Column(nullable = false)
    val level: Int,

    @Column(nullable = false)
    val rarity: Int,

    @Column(nullable = false)
    val constellations: Int,
)

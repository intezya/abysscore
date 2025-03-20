package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.model.entity.DraftCharacter
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class DraftCharacterDTO(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val element: String,

    @field:Min(1)
    @field:Max(90)
    val level: Int,

    @field:Min(4)
    @field:Max(5)
    val rarity: Int,

    @field:Min(0)
    @field:Max(6)
    val constellations: Int,
) {
    fun toEntity() = DraftCharacter(
        name = name,
        element = element,
        level = level,
        rarity = rarity,
        constellations = constellations,
    )

    constructor(draftCharacter: DraftCharacter) : this(
        name = draftCharacter.name,
        element = draftCharacter.element,
        level = draftCharacter.level,
        rarity = draftCharacter.rarity,
        constellations = draftCharacter.constellations,
    )
}

fun DraftCharacter.toDTO(): DraftCharacterDTO = DraftCharacterDTO(this)

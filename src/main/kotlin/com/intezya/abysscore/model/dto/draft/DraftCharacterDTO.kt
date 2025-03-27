package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.model.entity.DraftCharacter
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

private const val CHARACTER_MINIMUM_LEVEL = 1L
private const val CHARACTER_MAXIMUM_LEVEL = 90L
private const val CHARACTER_MINIMUM_RARITY = 4L
private const val CHARACTER_MAXIMUM_RARITY = 5L
private const val CHARACTER_MINIMUM_CONSTELLATIONS = 0L
private const val CHARACTER_MAXIMUM_CONSTELLATIONS = 6L

data class DraftCharacterDTO(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val element: String,

    @field:Min(CHARACTER_MINIMUM_LEVEL)
    @field:Max(CHARACTER_MAXIMUM_LEVEL)
    val level: Int,

    @field:Min(CHARACTER_MINIMUM_RARITY)
    @field:Max(CHARACTER_MAXIMUM_RARITY)
    val rarity: Int,

    @field:Min(CHARACTER_MINIMUM_CONSTELLATIONS)
    @field:Max(CHARACTER_MAXIMUM_CONSTELLATIONS)
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

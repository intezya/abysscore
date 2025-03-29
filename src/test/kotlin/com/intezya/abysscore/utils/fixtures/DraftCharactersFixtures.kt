package com.intezya.abysscore.utils.fixtures

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import java.util.*
import kotlin.random.Random

object DraftCharactersFixtures {
    fun createDraftCharacter() = DraftCharacterDTO(
        name = UUID.randomUUID().toString(),
        element = UUID.randomUUID().toString(),
        level = Random.nextInt(1, 90),
        rarity = Random.nextInt(4, 5),
        constellations = Random.nextInt(0, 6),
    )

    fun createDraftCharacters(n: Int) = List(n) { createDraftCharacter() }
}

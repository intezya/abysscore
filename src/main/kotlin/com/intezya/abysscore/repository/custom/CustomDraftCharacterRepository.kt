package com.intezya.abysscore.repository.custom

import com.intezya.abysscore.model.entity.DraftCharacter

interface CustomDraftCharacterRepository {
    fun saveAllIgnoringDuplicates(characters: List<DraftCharacter>): List<DraftCharacter>
}

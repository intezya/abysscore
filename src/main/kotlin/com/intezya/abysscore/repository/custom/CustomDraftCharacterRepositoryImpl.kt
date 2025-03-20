package com.intezya.abysscore.repository.custom

import com.intezya.abysscore.model.entity.DraftCharacter
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

// Used for function optimization
private const val BATCH_SIZE_LIMIT = 50

@Repository
class CustomDraftCharacterRepositoryImpl(
    private val entityManager: EntityManager,
) : CustomDraftCharacterRepository {
    override fun saveAllIgnoringDuplicates(characters: List<DraftCharacter>): List<DraftCharacter> {
        val result = mutableListOf<DraftCharacter>()

        characters.forEachIndexed { index, character ->
            val managedEntity = if (character.id != 0L) {
                entityManager.merge(character)
            } else {
                entityManager.persist(character)
                character
            }

            result.add(managedEntity)

            if (index > 0 && index % BATCH_SIZE_LIMIT == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }

        entityManager.flush()
        return result
    }
}

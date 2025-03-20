package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.DraftCharacter
import com.intezya.abysscore.repository.custom.CustomDraftCharacterRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DraftCharacterRepository :
    JpaRepository<DraftCharacter, Long>,
    CustomDraftCharacterRepository

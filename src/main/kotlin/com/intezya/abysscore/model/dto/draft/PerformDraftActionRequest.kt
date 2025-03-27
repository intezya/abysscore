package com.intezya.abysscore.model.dto.draft

import jakarta.validation.constraints.NotBlank

data class PerformDraftActionRequest(
    @field:NotBlank
    val characterName: String,
)

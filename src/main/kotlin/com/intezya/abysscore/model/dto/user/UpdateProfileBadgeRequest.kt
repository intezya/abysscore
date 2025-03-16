package com.intezya.abysscore.model.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class UpdateProfileBadgeRequest(
    @field:NotNull
    @field:JsonProperty("itemId")
    val itemId: Long,
)

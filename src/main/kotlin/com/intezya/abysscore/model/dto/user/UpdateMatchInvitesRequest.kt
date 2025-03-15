package com.intezya.abysscore.model.dto.user

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class UpdateMatchInvitesRequest(
    @field:NotNull
    @field:JsonProperty("receive_match_invites")
    val receiveMatchInvites: Boolean,
)

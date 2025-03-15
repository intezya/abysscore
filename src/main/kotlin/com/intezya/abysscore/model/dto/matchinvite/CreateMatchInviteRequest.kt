package com.intezya.abysscore.model.dto.matchinvite

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateMatchInviteRequest(
    @field:JsonProperty("invitee_username")
    val inviteeUsername: String,
)

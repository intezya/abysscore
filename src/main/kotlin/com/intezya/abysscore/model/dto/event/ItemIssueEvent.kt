package com.intezya.abysscore.model.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class ItemIssueEvent(
    @field:JsonProperty("item_id") val itemId: Long,
    @field:JsonProperty("receiver_id") val receiverId: Long,
    @field:JsonProperty("issued_by") val issuedBy: Long,
    @field:JsonProperty("timestamp") val timestamp: Instant = Instant.now(),
)

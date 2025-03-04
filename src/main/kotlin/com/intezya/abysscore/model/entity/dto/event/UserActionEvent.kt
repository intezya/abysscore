package com.intezya.abysscore.model.entity.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.abysscore.enum.UserActionEventType
import java.time.Instant

data class UserActionEvent(
    @field:JsonProperty("username") val username: String,
    @field:JsonProperty("ip") val ip: String,
    @field:JsonProperty("event_type") val eventType: UserActionEventType,
    @field:JsonProperty("is_success") val isSuccess: Boolean,
    @field:JsonProperty("hwid") val hwid: String,
    @field:JsonProperty("timestamp") val timestamp: Instant = Instant.now(),
)

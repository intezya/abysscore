package com.intezya.abysscore.model.dto.matchprocess

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class SubmitRoomResultRequest(
    @field:Min(1)
    @field:Max(3)
    @field:JsonProperty("room_number")
    val roomNumber: Int,
    @field:Min(0)
    val time: Int,
)

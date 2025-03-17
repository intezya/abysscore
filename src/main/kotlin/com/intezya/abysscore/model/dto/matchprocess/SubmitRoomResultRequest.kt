package com.intezya.abysscore.model.dto.matchprocess

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class SubmitRoomResultRequest(
    @field:Min(1)
    @field:Max(3)
    val roomNumber: Int,
    @field:Min(0)
    val time: Int,
)

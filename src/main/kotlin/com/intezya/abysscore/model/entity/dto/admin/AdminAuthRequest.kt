package com.intezya.abysscore.model.entity.dto.admin

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AdminAuthRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val username: String,
    @field:NotBlank
    @field:Size(min = 8)
    val password: String,
    @field:NotBlank
    // TODO: length
    val hwid: String,
)

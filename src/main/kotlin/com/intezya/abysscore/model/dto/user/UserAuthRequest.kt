package com.intezya.abysscore.model.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserAuthRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val username: String,
    @field:NotBlank
    @field:Size(min = 8, max = 256)
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d_-]{8,256}\$",
        message = "Password must be between 8 and 256 characters, and include at least one uppercase letter, one lowercase letter, and one digit"
    )
    val password: String,
    @field:NotBlank
    // TODO: length
    val hwid: String
)

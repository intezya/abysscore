package com.intezya.abysscore.dto.user

data class UserAuthInfoDTO(
    val id: Long,
    val username: String,
    val hwid: String,
    val accessLevel: Int = -1,
)

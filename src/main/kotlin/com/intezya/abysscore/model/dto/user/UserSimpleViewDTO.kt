package com.intezya.abysscore.model.dto.user

import com.intezya.abysscore.model.entity.User
import java.time.LocalDateTime

data class UserSimpleViewDTO(
    val id: Long,
    val username: String,
    val createdAt: LocalDateTime,
) {
    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        createdAt = user.createdAt,
    )

    constructor(user: UserDTO) : this(
        id = user.id,
        username = user.username,
        createdAt = user.createdAt,
    )
}

fun UserDTO.simpleView(): UserSimpleViewDTO = UserSimpleViewDTO(this)

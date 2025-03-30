package com.intezya.abysscore.model.dto.user

import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.model.dto.useritem.toDTO
import com.intezya.abysscore.model.entity.user.User
import java.time.LocalDateTime

data class UserSimpleViewDTO(
    val id: Long,
    val username: String,
    val currentBadge: UserItemDTO?,
    val createdAt: LocalDateTime,
) {
    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        currentBadge = user.currentBadge?.toDTO(),
        createdAt = user.createdAt,
    )
}

fun User.toSimpleView(): UserSimpleViewDTO = UserSimpleViewDTO(this)

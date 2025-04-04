package com.intezya.abysscore.model.dto.user

import com.intezya.abysscore.model.dto.match.CurrentMatchDTO
import com.intezya.abysscore.model.dto.match.toCurrentMatchDTO
import com.intezya.abysscore.model.dto.statistic.UserGlobalStatisticDTO
import com.intezya.abysscore.model.dto.statistic.toDTO
import com.intezya.abysscore.model.dto.useritem.UserItemViewDTO
import com.intezya.abysscore.model.dto.useritem.toViewDTO
import com.intezya.abysscore.model.entity.user.User
import java.time.LocalDateTime

data class UserDTO(
    val id: Long,
    val username: String,
    val createdAt: LocalDateTime,
    val inventory: List<UserItemViewDTO>,
    val receiveMatchInvites: Boolean,
    val currentMatch: CurrentMatchDTO?,
    val globalStatistic: UserGlobalStatisticDTO,
    val currentBadge: UserItemViewDTO?,
    val avatarUrl: String?,
) {
    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        createdAt = user.createdAt,
        inventory = user.items.map { it.toViewDTO() },
        receiveMatchInvites = user.receiveMatchInvites,
        currentMatch = user.currentMatch?.toCurrentMatchDTO(),
        globalStatistic = user.globalStatistic.toDTO(),
        currentBadge = user.currentBadge?.toViewDTO(),
        avatarUrl = user.avatarUrl,
    )
}

fun User.toDTO(): UserDTO = UserDTO(this)

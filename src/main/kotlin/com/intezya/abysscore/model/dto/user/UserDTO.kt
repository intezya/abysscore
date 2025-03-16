package com.intezya.abysscore.model.dto.user

import com.intezya.abysscore.model.dto.match.MatchDTO
import com.intezya.abysscore.model.dto.match.toDTO
import com.intezya.abysscore.model.dto.statistic.UserGlobalStatisticDTO
import com.intezya.abysscore.model.dto.statistic.toDTO
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.model.dto.useritem.toDTO
import com.intezya.abysscore.model.entity.User
import java.time.LocalDateTime

data class UserDTO(
    val id: Long,
    val username: String,
    val createdAt: LocalDateTime,
    val inventory: Set<UserItemDTO>,
    val receiveMatchInvites: Boolean,
    val currentMatch: MatchDTO?,
    val globalStatistic: UserGlobalStatisticDTO,
    val currentBadge: UserItemDTO?,
) {
    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        createdAt = user.createdAt,
        inventory = user.items.map { it.toDTO() }.toSet(),
        receiveMatchInvites = user.receiveMatchInvites,
        currentMatch = user.currentMatch?.toDTO(),
        globalStatistic = user.globalStatistic.toDTO(),
        currentBadge = user.currentBadge?.toDTO(),
    )
}

fun User.toDTO(): UserDTO = UserDTO(this)

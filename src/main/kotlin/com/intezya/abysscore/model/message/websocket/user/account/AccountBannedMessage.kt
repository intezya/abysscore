package com.intezya.abysscore.model.message.websocket.user.account

import java.time.LocalDateTime

data class AccountBannedMessage(val bannedUntil: LocalDateTime, val reason: String?) : BaseAccountMessage()

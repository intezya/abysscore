package com.intezya.abysscore.model.message.websocket

data class UserLoggedInMessage(
    val username: String,
    val currentOnline: Int,
) {
    val message: String = "User logged in"
}

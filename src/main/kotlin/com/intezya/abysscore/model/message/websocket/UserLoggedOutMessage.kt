package com.intezya.abysscore.model.message.websocket

data class UserLoggedOutMessage(
    val username: String,
    val currentOnline: Int,
) {
    val message: String = "User logged out"
}

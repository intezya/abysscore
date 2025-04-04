package com.intezya.abysscore.model.message.websocket.user.action

data class UserLoggedInMessage(val username: String, val currentOnline: Int) : BaseUserActionMessage() {
    val message: String = "User logged in"
}

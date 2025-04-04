package com.intezya.abysscore.listener

import com.intezya.abysscore.event.user.action.UserConnectedEvent
import com.intezya.abysscore.event.user.action.UserDisconnectedEvent
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserActionEventListener(private val websocketNotificationService: WebsocketNotificationService) {
    @EventListener
    fun userLoggedIn(event: UserConnectedEvent) {
        websocketNotificationService.userLoggedIn(event.user.id, event.user.username)
    }

    @EventListener
    fun userLoggedOut(event: UserDisconnectedEvent) {
        websocketNotificationService.userLoggedOut(event.user.id, event.user.username)
    }
}

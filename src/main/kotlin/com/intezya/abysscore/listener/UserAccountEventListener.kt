package com.intezya.abysscore.listener

import com.intezya.abysscore.event.user.account.AccountBannedEvent
import com.intezya.abysscore.service.WebsocketNotificationService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserAccountEventListener(private val websocketNotificationService: WebsocketNotificationService) {
    @EventListener()
    fun onAccountBan(event: AccountBannedEvent) {
        if (event.user.bannedUntil != null) {
            websocketNotificationService.sendAccountBanned(
                event.user.id,
                event.user.bannedUntil!!,
                event.user.banReason,
            )
        }
    }
}

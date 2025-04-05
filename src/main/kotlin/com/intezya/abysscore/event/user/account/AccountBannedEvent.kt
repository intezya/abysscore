package com.intezya.abysscore.event.user.account

import com.intezya.abysscore.model.entity.user.User
import org.springframework.context.ApplicationEvent

class AccountBannedEvent(source: Any, val user: User) : ApplicationEvent(source)

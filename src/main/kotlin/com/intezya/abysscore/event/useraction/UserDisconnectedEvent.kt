package com.intezya.abysscore.event.useraction

import com.intezya.abysscore.model.entity.user.User
import org.springframework.context.ApplicationEvent

class UserDisconnectedEvent(source: Any, val user: User) : ApplicationEvent(source)

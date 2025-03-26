package com.intezya.abysscore.event

import com.intezya.abysscore.model.entity.User
import org.springframework.context.ApplicationEvent

class UserDisconnectedEvent(
    source: Any,
    val user: User,
) : ApplicationEvent(source)

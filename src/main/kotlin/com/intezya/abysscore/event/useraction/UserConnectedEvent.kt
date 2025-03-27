package com.intezya.abysscore.event.useraction

import com.intezya.abysscore.model.entity.User
import org.springframework.context.ApplicationEvent

class UserConnectedEvent(source: Any, val user: User) : ApplicationEvent(source)

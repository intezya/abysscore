package com.intezya.abysscore.event.match.invite

import com.intezya.abysscore.model.entity.user.User
import org.springframework.context.ApplicationEvent

class InviteRejectedEvent(source: Any, val inviteId: Long, val invitee: User, val inviter: User) :
    ApplicationEvent(source)

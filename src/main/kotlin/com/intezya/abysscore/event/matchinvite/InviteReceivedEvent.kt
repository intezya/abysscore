package com.intezya.abysscore.event.matchinvite

import com.intezya.abysscore.model.entity.User
import org.springframework.context.ApplicationEvent

class InviteReceivedEvent(source: Any, val inviteId: Long, val invitee: User, val inviter: User) :
    ApplicationEvent(source)

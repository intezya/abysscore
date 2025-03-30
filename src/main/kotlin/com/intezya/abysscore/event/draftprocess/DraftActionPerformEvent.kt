package com.intezya.abysscore.event.draftprocess

import com.intezya.abysscore.model.entity.DraftAction
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User
import org.springframework.context.ApplicationEvent

class DraftActionPerformEvent(
    source: Any,
    val player: User,
    val match: Match,
    val action: DraftAction,
) : ApplicationEvent(source)

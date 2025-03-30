package com.intezya.abysscore.event.draftprocess

import com.intezya.abysscore.model.entity.draft.DraftAction
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import org.springframework.context.ApplicationEvent

class DraftActionPerformEvent(
    source: Any,
    val player: User,
    val match: Match,
    val action: DraftAction,
) : ApplicationEvent(source)

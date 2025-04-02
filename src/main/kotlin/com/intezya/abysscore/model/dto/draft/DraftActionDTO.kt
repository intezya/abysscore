package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.draft.DraftAction
import java.time.LocalDateTime

data class DraftActionDTO(
    val user: UserSimpleViewDTO,
    val actionType: DraftActionType,
    val characterName: String?,
    val timestamp: LocalDateTime,
    val isTimeoutAction: Boolean,
) {
    constructor(draftAction: DraftAction) : this(
        user = draftAction.user.toSimpleView(),
        actionType = draftAction.actionType,
        characterName = draftAction.characterName,
        timestamp = draftAction.timestamp,
        isTimeoutAction = draftAction.isTimeoutAction,
    )
}

fun DraftAction.toDTO(): DraftActionDTO = DraftActionDTO(this)

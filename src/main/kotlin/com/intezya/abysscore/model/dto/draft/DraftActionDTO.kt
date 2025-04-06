package com.intezya.abysscore.model.dto.draft

import com.intezya.abysscore.model.dto.user.UserSimpleViewDTO
import com.intezya.abysscore.model.dto.user.toSimpleView
import com.intezya.abysscore.model.entity.draft.DraftAction
import java.time.LocalDateTime

data class DraftActionDTO(
    val player: UserSimpleViewDTO,
    val isPick: Boolean,
    val characterName: String,
    val createdAt: LocalDateTime,
) {
    constructor(draftAction: DraftAction) : this(
        player = draftAction.player.toSimpleView(),
        isPick = draftAction.isPick,
        characterName = draftAction.characterName,
        createdAt = draftAction.createdAt,
    )
}

fun DraftAction.toDTO(): DraftActionDTO = DraftActionDTO(this)

package com.intezya.abysscore.model.message.websocket.draft.process

import com.intezya.abysscore.model.dto.draft.DraftActionDTO
import com.intezya.abysscore.model.message.websocket.Messages

data class DraftActionPerformMessage(val message: String = Messages.DRAFT_ACTION_PERFORMED, val action: DraftActionDTO)

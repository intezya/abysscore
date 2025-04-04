package com.intezya.abysscore.model.message.websocket.match.process

import com.intezya.abysscore.model.message.websocket.Messages

data class SubmitRoomResultMessage(val roomNumber: Int, val result: Int) : BaseMatchProcessMessage() {
    val message = Messages.MATCH_RESULT_SUBMIT
}

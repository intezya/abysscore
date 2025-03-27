package com.intezya.abysscore.exception

import java.time.LocalDateTime

data class ErrorResponse(val status: Int, val message: String, val timestamp: LocalDateTime)

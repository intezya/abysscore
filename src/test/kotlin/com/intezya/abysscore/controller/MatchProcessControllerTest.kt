package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MatchProcessControllerTest : BaseApiTest() {
    @Nested
    inner class MatchSubmitRetry {
        @Test
        fun `should submit retry`() {
            val createResult = createMatch()

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            for (i in 1..5) {
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/match/current/submit-retry")
                    }.Then {
                        statusCode(200)
                        body("id", notNullValue())
                    }
            }
        }

        @Test
        fun `should not send a retry if there are too many of them`() {
            val createResult = createMatch()

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            for (i in 1..5) {
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/match/current/submit-retry")
                    }.Then {
                        statusCode(HttpStatus.OK.value())
                        body("id", notNullValue())
                    }
            }

            authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                .body(request)
                .When {
                    post("/match/current/submit-retry")
                }.Then {
                    statusCode(HttpStatus.CONFLICT.value())
                }
        }

        @Test
        fun `shouldn't work if user not in match`() {
            val (_, token) = createUser()

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            authenticatedRequest(token)
                .body(request)
                .When {
                    post("/match/current/submit-retry")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }
    }

    @Nested
    inner class MatchSubmitResult {
        @Test
        fun `should submit result`() {
            val createResult = createMatch()

            for (i in 1..3) {
                val request = SubmitRoomResultRequest(
                    roomNumber = i,
                    time = 20,
                )

                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/match/current/submit-result")
                    }.Then {
                        statusCode(200)
                        body("id", notNullValue())
                    }
            }
        }

        @Test
        fun `should not send a result if there already result in number`() {
            val createResult = createMatch()
            for (i in 1..3) {
                val request = SubmitRoomResultRequest(
                    roomNumber = i,
                    time = 20,
                )
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/match/current/submit-result")
                    }.Then {
                        statusCode(HttpStatus.OK.value())
                        body("id", notNullValue())
                    }

                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/match/current/submit-result")
                    }.Then {
                        statusCode(HttpStatus.CONFLICT.value())
                    }
            }
        }

        @Test
        fun `shouldn't work if user not in match`() {
            val (_, token) = createUser()

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            authenticatedRequest(token)
                .body(request)
                .When {
                    post("/match/current/submit-result")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }
    }
}

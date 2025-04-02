package com.intezya.abysscore.integration.controller

import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.utils.fixtures.MatchFixtures
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

class MatchProcessControllerIntegrationTest : BaseApiTest() {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Nested
    inner class MatchSubmitRetry {
        @Test
        fun `should submit retry`() {
            val createResult = createMatch()

            val savedMatch = createResult.match
            savedMatch.apply {
                status = MatchStatus.ACTIVE
            }
            matchRepository.save(savedMatch)

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            for (i in 1..5) {
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/matches/current/process/submit-retry")
                    }.Then {
                        statusCode(200)
                        body("id", CoreMatchers.notNullValue())
                    }
            }
        }

        @Test
        fun `should not send a retry if there are too many of them`() {
            val createResult = createMatch()

            val savedMatch = createResult.match
            savedMatch.apply {
                status = MatchStatus.ACTIVE
            }
            matchRepository.save(savedMatch)

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            for (i in 1..5) {
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/matches/current/process/submit-retry")
                    }.Then {
                        statusCode(HttpStatus.OK.value())
                        body("id", CoreMatchers.notNullValue())
                    }
            }

            authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                .body(request)
                .When {
                    post("/matches/current/process/submit-retry")
                }.Then {
                    statusCode(HttpStatus.CONFLICT.value())
                }
        }

        @Test
        fun `shouldn't work if user not in match`() {
            val (user, token) = generateUserWithToken()

            val match = MatchFixtures.createDefaultMatch(user1 = user)
            userRepository.save(match.player2)
            matchRepository.save(match)
            user.currentMatch = match
            userRepository.save(user)

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            authenticatedRequest(token)
                .body(request)
                .When {
                    post("/matches/current/process/submit-retry")
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

            val savedMatch = createResult.match
            savedMatch.apply {
                status = MatchStatus.ACTIVE
            }
            matchRepository.save(savedMatch)

            for (i in 1..3) {
                val request = SubmitRoomResultRequest(
                    roomNumber = i,
                    time = 20,
                )

                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/matches/current/process/submit-result")
                    }.Then {
                        statusCode(200)
                        body("id", CoreMatchers.notNullValue())
                    }
            }
        }

        @Test
        fun `should not send a result if there already result in number`() {
            val createResult = createMatch()

            val savedMatch = createResult.match
            savedMatch.apply {
                status = MatchStatus.ACTIVE
            }
            matchRepository.save(savedMatch)

            for (i in 1..3) {
                val request = SubmitRoomResultRequest(
                    roomNumber = i,
                    time = 20,
                )
                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/matches/current/process/submit-result")
                    }.Then {
                        statusCode(HttpStatus.OK.value())
                        body("id", CoreMatchers.notNullValue())
                    }

                authenticatedRequest(jwtUtils.generateToken(createResult.player1))
                    .body(request)
                    .When {
                        post("/matches/current/process/submit-result")
                    }.Then {
                        statusCode(HttpStatus.CONFLICT.value())
                    }
            }
        }

        @Test
        fun `shouldn't work if user not in match`() {
            val (user, token) = generateUserWithToken()

            val match = MatchFixtures.createDefaultMatch(user1 = user)
            userRepository.save(match.player2)
            matchRepository.save(match)
            user.currentMatch = match
            userRepository.save(user)

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            authenticatedRequest(token)
                .body(request)
                .When {
                    post("/matches/current/process/submit-result")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        @Test
        fun `shouldn't work if match not in active stage`() {
            val (user, token) = generateUserWithToken()

            val match = MatchFixtures.createDefaultMatch(user1 = user)
            userRepository.save(match.player2)
            matchRepository.save(match)
            user.currentMatch = match
            userRepository.save(user)

            val request = SubmitRoomResultRequest(
                roomNumber = 1,
                time = 20,
            )

            authenticatedRequest(token)
                .body(request)
                .When {
                    post("/matches/current/process/submit-result")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }
    }

    @Nested
    inner class MatchTimeoutTest {
        @Test
        fun `should timeout match for both players`() {
            val createResult = createMatch()

            updateMatchCreatedAt(createResult.match.id, LocalDateTime.now().minusDays(1))

            val token = jwtUtils.generateToken(createResult.player1)
            authenticatedRequest(token)
                .When {
                    post("/matches/current/process/submit-result")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        private fun updateMatchCreatedAt(matchId: Long, createdAt: LocalDateTime) {
            val updateSql = "UPDATE matches SET created_at = ? WHERE id = ?"

            jdbcTemplate.update(
                updateSql,
                createdAt,
                matchId,
            )
        }
    }
}

package com.intezya.abysscore.integration.controller

import com.intezya.abysscore.constants.MATCH_INVITES_ENDPOINT
import com.intezya.abysscore.integration.BaseApiTest
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MatchInviteControllerIntegrationTest : BaseApiTest() {
    @Nested
    inner class CreateMatchInvite {
        @Test
        fun `should create user invite when invitee accepts invites`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val request = createUserInviteRequest(invitee.username)

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.CREATED.value())
                }
        }

        @Test
        fun `should not create user invite when invitee has disabled invites`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, _) = generateUserWithToken()

            val request = createUserInviteRequest(invitee.username)

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }

        @Test
        fun `should not create user invite if invitee does not exist`() {
            val (_, inviterToken) = generateUserWithToken()
            val request = createUserInviteRequest("nonexistent_username")

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `should not create user invite if invite already exists`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val request = createUserInviteRequest(invitee.username)

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.CREATED.value())
                }

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.CONFLICT.value())
                }
        }

        @Test
        fun `should not create user invite for yourself`() {
            val (inviter, inviterToken) = generateUserWithToken()
            setAcceptInvites(inviterToken, true)
            val request = createUserInviteRequest(inviter.username)

            authenticatedRequest(inviterToken)
                .body(request)
                .When {
                    post(MATCH_INVITES_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }
    }

    @Nested
    inner class AcceptMatchInvite {
        @Test
        fun `should accept user invite`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val inviteId = createInvite(inviterToken, invitee.username)

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("id", CoreMatchers.notNullValue())
                    body("player1.id", CoreMatchers.notNullValue())
                    body("player1.username", CoreMatchers.notNullValue())
                    body("player2.id", CoreMatchers.notNullValue())
                    body("player2.username", CoreMatchers.notNullValue())
                }
        }

        @Test
        fun `should not accept user invite if invite already accepted`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val inviteId = createInvite(inviterToken, invitee.username)

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                }

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `should not accept user invite if invite does not exist`() {
            val (_, inviteeToken) = generateUserWithToken()
            val nonExistentInviteId = 1L

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$nonExistentInviteId/accept")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }
    }

    @Nested
    inner class DeclineMatchInvite {
        @Test
        fun `should decline user invite`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val inviteId = createInvite(inviterToken, invitee.username)

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
                }.Then {
                    statusCode(HttpStatus.NO_CONTENT.value())
                }
        }

        @Test
        fun `should not decline user invite if invite does not exist`() {
            val (_, inviteeToken) = generateUserWithToken()
            val nonExistentInviteId = 1L

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$nonExistentInviteId/decline")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `should not decline user invite if invite already declined`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val inviteId = createInvite(inviterToken, invitee.username)

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
                }.Then {
                    statusCode(HttpStatus.NO_CONTENT.value())
                }

            authenticatedRequest(inviteeToken)
                .When {
                    post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }
    }
}

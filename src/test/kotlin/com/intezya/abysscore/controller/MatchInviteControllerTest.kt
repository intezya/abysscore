package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

private const val MATCH_INVITES_ENDPOINT = "/match/invites"
private const val USER_PREFERENCES_INVITES_ENDPOINT = "/users/preferences/invites"

@DisplayName("Match Invite Controller Tests")
class MatchInviteControllerTest : BaseApiTest() {
    @Nested
    @DisplayName("Create Match Invite")
    inner class CreateMatchInvite {
        @Test
        fun `should create user invite when invitee accepts invites`() {
            val (inviter, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val request = createUserInviteRequest(invitee.username)

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            }
        }

        @Test
        fun `should not create user invite when invitee has disabled invites`() {
            val (inviter, inviterToken) = generateUserWithToken()
            val (invitee, _) = generateUserWithToken()

            val request = createUserInviteRequest(invitee.username)

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.FORBIDDEN.value())
            }
        }

        @Test
        fun `should not create user invite if invitee does not exist`() {
            val (_, inviterToken) = generateUserWithToken()
            val request = createUserInviteRequest("nonexistent_username")

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }

        @Test
        fun `should not create user invite if invite already exists`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val request = createUserInviteRequest(invitee.username)

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            }

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CONFLICT.value())
            }
        }

        @Test
        fun `should not create user invite for yourself`() {
            val (inviter, inviterToken) = generateUserWithToken()
            setAcceptInvites(inviterToken, true)
            val request = createUserInviteRequest(inviter.username)

            Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.value())
            }
        }
    }

    @Nested
    @DisplayName("Accept Match Invite")
    inner class AcceptMatchInvite {
        @Test
        fun `should accept user invite`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val createInviteRequest = createUserInviteRequest(invitee.username)

            val response = Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(createInviteRequest)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            } Extract {
                body().jsonPath().getMap<String, Any>("")
            }

            val inviteId = (response["id"] as Int).toLong()

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
            } Then {
                statusCode(HttpStatus.OK.value())
            }

            // TODO: check that match was created
        }

        @Test
        fun `should not accept user invite if invite already accepted`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val createInviteRequest = createUserInviteRequest(invitee.username)

            val invite = Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(createInviteRequest)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            } Extract {
                body().jsonPath().getMap<String, Any>("")
            }

            val inviteId = (invite["id"] as Int).toLong()

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
            } Then {
                statusCode(HttpStatus.OK.value())
            }

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/accept")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }

        @Test
        fun `should not accept user invite if invite does not exist`() {
            val (_, inviteeToken) = generateUserWithToken()
            val nonExistentInviteId = 1L

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$nonExistentInviteId/accept")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }
    }

    @Nested
    @DisplayName("Decline Match Invite")
    inner class DeclineMatchInvite {
        @Test
        fun `should decline user invite`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val createInviteRequest = createUserInviteRequest(invitee.username)

            val invite = Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(createInviteRequest)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            } Extract {
                body().jsonPath().getMap<String, Any>("")
            }
            val inviteId = (invite["id"] as Int).toLong()

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.value())
            }
        }

        @Test
        fun `should not decline user invite if invite does not exist`() {
            val (_, inviteeToken) = generateUserWithToken()
            val nonExistentInviteId = 1L

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$nonExistentInviteId/decline")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }

        @Test
        fun `should not decline user invite if invite already declined`() {
            val (_, inviterToken) = generateUserWithToken()
            val (invitee, inviteeToken) = generateUserWithToken()

            setAcceptInvites(inviteeToken, true)
            val createInviteRequest = createUserInviteRequest(invitee.username)

            val invite = Given {
                header("Authorization", "Bearer $inviterToken")
                contentType(ContentType.JSON)
                body(createInviteRequest)
            } When {
                post(MATCH_INVITES_ENDPOINT)
            } Then {
                statusCode(HttpStatus.CREATED.value())
            } Extract {
                body().jsonPath().getMap<String, Any>("")
            }
            val inviteId = (invite["id"] as Int).toLong()

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.value())
            }

            Given {
                header("Authorization", "Bearer $inviteeToken")
                contentType(ContentType.JSON)
            } When {
                post("$MATCH_INVITES_ENDPOINT/$inviteId/decline")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }
    }

    private fun createUserInviteRequest(inviteeUsername: String) = CreateMatchInviteRequest(inviteeUsername = inviteeUsername)

    private fun setAcceptInvites(token: String, accept: Boolean) {
        val request = UpdateMatchInvitesRequest(receiveMatchInvites = accept)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            patch(USER_PREFERENCES_INVITES_ENDPOINT)
        } Then {
            statusCode(HttpStatus.OK.value())
        }
    }
}

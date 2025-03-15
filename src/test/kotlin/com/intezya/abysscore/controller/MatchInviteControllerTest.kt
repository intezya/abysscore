package com.intezya.abysscore.controller

import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MatchInviteControllerTest : BaseApiTest() {
    @Nested
    inner class CreateMatchInvite {
        @Test
        fun `should create user invite`() {
            val inviter = generateUserWithToken()
            val invitee = generateUserWithToken()

            setAcceptInvites(invitee.second, true)

            val request = createUserInviteRequest(invitee.first.username)

            Given {
                header("Authorization", "Bearer ${inviter.second}")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/match/invites")
            } Then {
                statusCode(HttpStatus.CREATED.value())
            }
        }

        @Test
        fun `shouldn't create user invite when invitee has disabled invites`() {
            val inviter = generateUserWithToken()
            val invitee = generateUserWithToken() // Invites disabled by default

            val request = createUserInviteRequest(invitee.first.username)

            Given {
                header("Authorization", "Bearer ${inviter.second}")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/match/invites")
            } Then {
                statusCode(HttpStatus.FORBIDDEN.value())
            }
        }

        @Test
        fun `shouldn't create user invite if invitee doesn't exists`() {
            val inviter = generateUserWithToken()

            val request = createUserInviteRequest("username")

            Given {
                header("Authorization", "Bearer ${inviter.second}")
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/match/invites")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.value())
            }
        }
    }

    fun createUserInviteRequest(inviteeUsername: String) = CreateMatchInviteRequest(inviteeUsername = inviteeUsername)

    fun setAcceptInvites(token: String, accept: Boolean) {
        val request = UpdateMatchInvitesRequest(receiveMatchInvites = accept)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            patch("/users/preferences/invites")
        } Then {
            statusCode(HttpStatus.OK.value())
        }
    }
}

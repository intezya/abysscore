package com.intezya.abysscore.integrationTest

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.*

class UserControllerTest : BaseApiTest() {
    @Nested
    inner class UserInfo {
        @Test
        fun `should get user info by token`() {
            val token = generateToken()
            val username = jwtUtils.extractUsername(token)

            assertTrue(jwtUtils.isTokenValid(token))

            authenticatedRequest(token)
                .When {
                    get("/users/me")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    contentType(ContentType.JSON)
                    body("id", notNullValue())
                    body("username", equalTo(username))
                    body("created_at", notNullValue())
                }
        }

        @Test
        fun `shouldn't get user info with invalid token`() {
            val token = jwtUtils.generateToken(user = User())
            val username = jwtUtils.extractUsername(token)

            authenticatedRequest(token)
                .When {
                    get("/users/me")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                    contentType(ContentType.JSON)
                }
        }

        // TODO: add test with invalid token
    }

    @Nested
    inner class GetAllUsers {
        @Test
        fun `get all should return list of users`() {
            val token = generateToken(AccessLevel.VIEW_ALL_USERS)
            val n = 100

            repeat(n) {
                val request = RandomProvider.constructAuthRequest(username = UUID.randomUUID().toString().take(16))
                userService.create(request)
            }

            val response = authenticatedRequest(token)
                .When {
                    get("/users")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content", notNullValue())
                }.Extract {
                    response().jsonPath()
                }

            val content = response.getList<Map<String, Any>>("content")
            val page = response.getMap<String, Any>("page")
            val totalElements = page["total_elements"].toString().toInt()
            val size = page["size"].toString().toInt()

            assertEquals(n + 1, totalElements)
            if (totalElements < size) {
                assertEquals(totalElements, content.size)
            } else {
                assertEquals(size, content.size)
            }
        }

        @Test
        fun `get all shouldn't work without required access level`() {
            val token = generateToken(AccessLevel.USER)

            authenticatedRequest(token)
                .When {
                    get("/users")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }
    }

    @Nested
    inner class SetInvitePreference {
        @Test
        fun `should set invites state`() {
            val (user, token) = generateUserWithToken()

            assertEquals(false, userService.findUserWithThrow(user.username).receiveMatchInvites)

            setAcceptInvites(token, accept = true)

            assertEquals(true, userService.findUserWithThrow(user.username).receiveMatchInvites)

            setAcceptInvites(token, accept = false)

            assertEquals(false, userService.findUserWithThrow(user.username).receiveMatchInvites)
        }
    }
}

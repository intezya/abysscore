package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
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

class UsersControllerTest : BaseApiTest() {
    @Nested
    inner class UserInfo {
        @Test
        fun `should get user info by token`() {
            val request = RandomProvider.constructAuthRequest()

            val token =
                Given {
                    contentType(ContentType.JSON)
                    body(request)
                } When {
                    post("/auth/register")
                } Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", notNullValue())
                } Extract {
                    path<String>("token")
                }

            assertTrue(jwtUtils.isTokenValid(token))

            Given {
                header("Authorization", "Bearer $token")
            } When {
                get("/users/me")
            } Then {
                statusCode(HttpStatus.OK.value())
                contentType(ContentType.JSON)
                body("id", notNullValue())
                body("username", equalTo(request.username))
                body("created_at", notNullValue())
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

            for (i in 1..n) {
                val request = RandomProvider.constructAuthRequest(username = UUID.randomUUID().toString().take(16))
                userService.create(request)
            }

            val response = Given {
                header("Authorization", "Bearer $token")
            } When {
                get("/users")
            } Then {
                statusCode(HttpStatus.OK.value())
                body("content", notNullValue())
            } Extract {
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

            Given {
                header("Authorization", "Bearer $token")
            } When {
                get("/users")
            } Then {
                statusCode(HttpStatus.FORBIDDEN.value())
            }
        }
    }
}

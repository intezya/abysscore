package com.intezya.abysscore.controller

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus
import java.util.*

class AuthControllerTest : BaseApiTest() {
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Registration {
        @Test
        fun `should register valid user`() {
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
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.providers.UserProvider#provideInvalidUsername")
        fun `shouldn't register user with invalid username`(invalidUsername: String) {
            val request = RandomProvider.constructAuthRequest(username = invalidUsername)

            Given {
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/auth/register")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.value())
            }
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.providers.UserProvider#provideInvalidPassword")
        fun `shouldn't register user with invalid password`(invalidPassword: String) {
            val request = RandomProvider.constructAuthRequest(password = invalidPassword)

            Given {
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/auth/register")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.value())
            }
        }

        @Test
        fun `shouldn't register user that already exists with username`() {
            val registered = RandomProvider.constructAuthRequest()

            userService.create(registered)

            val request = RandomProvider.constructAuthRequest(username = registered.username)

            Given {
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/auth/register")
            } Then {
                statusCode(HttpStatus.CONFLICT.value())
            }
        }

        @Test
        fun `shouldn't register user that already has account on device`() {
            val registered = RandomProvider.constructAuthRequest()

            userService.create(registered)

            val request = RandomProvider.constructAuthRequest(hwid = registered.hwid)

            Given {
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/auth/register")
            } Then {
                statusCode(HttpStatus.CONFLICT.value())
            }
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Login {
        @Test
        fun `should login valid user`() {
            val request = RandomProvider.constructAuthRequest()
            userService.create(request)

            val token =
                Given {
                    contentType(ContentType.JSON)
                    body(request)
                } When {
                    post("/auth/login")
                } Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", notNullValue())
                } Extract {
                    path<String>("token")
                }

            assertTrue(jwtUtils.isTokenValid(token))
        }

        @Test
        fun `shouldn't login user that not found`() {
            val request = RandomProvider.constructAuthRequest()

            Given {
                contentType(ContentType.JSON)
                body(request)
            } When {
                post("/auth/login")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }

        @Test
        fun `shouldn't login user with invalid password`() {
            val registerRequest = RandomProvider.constructAuthRequest()
            userService.create(registerRequest)

            val loginRequest = RandomProvider.constructAuthRequest(username = registerRequest.username)

            Given {
                contentType(ContentType.JSON)
                body(loginRequest)
            } When {
                post("/auth/login")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }

        @Test
        fun `shouldn't login user with invalid hwid`() {
            val registerRequest = RandomProvider.constructAuthRequest()
            userService.create(registerRequest)

            val loginRequest =
                RandomProvider.constructAuthRequest(
                    username = registerRequest.username,
                    password = registerRequest.password,
                )

            Given {
                contentType(ContentType.JSON)
                body(loginRequest)
            } When {
                post("/auth/login")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.providers.UserProvider#provideUsernameWithAnyCases")
        fun `should login user with any username case`(
            original: String,
            target: String,
        ) {
            val registerRequest = RandomProvider.constructAuthRequest(username = original)
            userService.create(registerRequest)

            val loginRequest =
                RandomProvider.constructAuthRequest(
                    username = target,
                    password = registerRequest.password,
                    hwid = registerRequest.hwid,
                )

            Given {
                contentType(ContentType.JSON)
                body(loginRequest)
            } When {
                post("/auth/login")
            } Then {
                statusCode(HttpStatus.OK.value())
            }
        }

        @Test
        fun `should login user that have null hwid`() {
            val userRegisterData = RandomProvider.constructUser()
            val user =
                User(
                    username = userRegisterData.username,
                    password = passwordUtils.hashPassword(userRegisterData.password),
                    hwid = null,
                )
            userRepository.save(user)

            val loginRequest =
                RandomProvider.constructAuthRequest(
                    username = user.username,
                    password = userRegisterData.password,
                    hwid = UUID.randomUUID().toString(),
                )

            val token =
                Given {
                    contentType(ContentType.JSON)
                    body(loginRequest)
                } When {
                    post("/auth/login")
                } Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", notNullValue())
                } Extract {
                    path<String>("token")
                }

            val authHWID = jwtUtils.extractHwid(token)
            assertTrue(passwordUtils.verifyHwid(loginRequest.hwid, authHWID))
        }
    }

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
}

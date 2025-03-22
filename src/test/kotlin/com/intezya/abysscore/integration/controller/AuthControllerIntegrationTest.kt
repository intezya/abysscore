package com.intezya.abysscore.integration.controller

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus
import java.util.*

class AuthControllerIntegrationTest : BaseApiTest() {
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Registration {
        @Test
        fun `should register valid user`() {
            val request = RandomProvider.constructAuthRequest()

            val token = jsonRequest()
                .body(request)
                .When {
                    post("/auth/register")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", CoreMatchers.notNullValue())
                }.Extract {
                    path<String>("token")
                }

            Assertions.assertTrue(jwtUtils.isTokenValid(token))
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.fixtures.UserProvider#provideInvalidUsername")
        fun `shouldn't register user with invalid username`(invalidUsername: String) {
            val request = RandomProvider.constructAuthRequest(username = invalidUsername)

            jsonRequest()
                .body(request)
                .When {
                    post("/auth/register")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.fixtures.UserProvider#provideInvalidPassword")
        fun `shouldn't register user with invalid password`(invalidPassword: String) {
            val request = RandomProvider.constructAuthRequest(password = invalidPassword)

            jsonRequest()
                .body(request)
                .When {
                    post("/auth/register")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        @Test
        fun `shouldn't register user that already exists with username`() {
            val registered = createUser()
            val request = RandomProvider.constructAuthRequest(username = registered.username)

            jsonRequest()
                .body(request)
                .When {
                    post("/auth/register")
                }.Then {
                    statusCode(HttpStatus.CONFLICT.value())
                }
        }

        @Test
        fun `shouldn't register user that already has account on device`() {
            val registered = createUser()
            val request = RandomProvider.constructAuthRequest(hwid = registered.hwid)

            jsonRequest()
                .body(request)
                .When {
                    post("/auth/register")
                }.Then {
                    statusCode(HttpStatus.CONFLICT.value())
                }
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class Login {
        @Test
        fun `should login valid user`() {
            val request = createUser()

            val token = jsonRequest()
                .body(request)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", CoreMatchers.notNullValue())
                }.Extract {
                    path<String>("token")
                }

            Assertions.assertTrue(jwtUtils.isTokenValid(token))
        }

        @Test
        fun `shouldn't login user that not found`() {
            val request = RandomProvider.constructAuthRequest()

            jsonRequest()
                .body(request)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.UNAUTHORIZED.value())
                }
        }

        @Test
        fun `shouldn't login user with invalid password`() {
            val registered = createUser()
            val loginRequest = RandomProvider.constructAuthRequest(username = registered.username)

            jsonRequest()
                .body(loginRequest)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.UNAUTHORIZED.value())
                }
        }

        @Test
        fun `shouldn't login user with invalid hwid`() {
            val registered = createUser()
            val loginRequest = RandomProvider.constructAuthRequest(
                username = registered.username,
                password = registered.password,
            )

            jsonRequest()
                .body(loginRequest)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.UNAUTHORIZED.value())
                }
        }

        @ParameterizedTest
        @MethodSource("com.intezya.abysscore.utils.fixtures.UserProvider#provideUsernameWithAnyCases")
        fun `should login user with any username case`(original: String, target: String) {
            val registerRequest = RandomProvider.constructAuthRequest(username = original)
            createUser(registerRequest)

            val loginRequest = RandomProvider.constructAuthRequest(
                username = target,
                password = registerRequest.password,
                hwid = registerRequest.hwid,
            )

            jsonRequest()
                .body(loginRequest)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                }
        }

        @Test
        fun `should login user that have null hwid`() {
            val userRegisterData = RandomProvider.constructUser()
            val user = User(
                username = userRegisterData.username,
                password = passwordUtils.hashPassword(userRegisterData.password),
                hwid = null,
            )
            userRepository.save(user)

            val loginRequest = RandomProvider.constructAuthRequest(
                username = user.username,
                password = userRegisterData.password,
                hwid = UUID.randomUUID().toString(),
            )

            val token = jsonRequest()
                .body(loginRequest)
                .When {
                    post("/auth/login")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("token", CoreMatchers.notNullValue())
                }.Extract {
                    path<String>("token")
                }

            val authHWID = jwtUtils.extractHwid(token)
            Assertions.assertTrue(passwordUtils.verifyHwid(loginRequest.hwid, authHWID))
        }
    }
}

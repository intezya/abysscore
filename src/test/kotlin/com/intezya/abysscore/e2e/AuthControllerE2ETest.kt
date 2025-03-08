package com.intezya.abysscore.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.service.AuthenticationService
import com.intezya.abysscore.utils.auth.AuthUtils
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.parsing.Parser
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
class AuthControllerE2ETest {
    @Autowired
    private lateinit var authUtils: AuthUtils

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @Test
    fun `should register valid user`() {
        val request = RandomProvider.constructAuthRequest()
        println(request)
        val token = Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/auth/register")
        } Then {
            statusCode(200)
            body("token", notNullValue())
        } Extract {
            path<String>("token")
        }

        assertTrue(authUtils.validateJwtToken(token))
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
            statusCode(400)
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
            statusCode(400)
        }
    }

    @Test
    fun `shouldn't register user that already exists with username`() {
        val registered = RandomProvider.constructAuthRequest()

        authenticationService.registerUser(registered, "someip")

        val request = RandomProvider.constructAuthRequest(username = registered.username)

        Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/auth/register")
        } Then {
            statusCode(409)
        }
    }

    @Test
    fun `shouldn't register user that already has account on device`() {
        val registered = RandomProvider.constructAuthRequest()

        authenticationService.registerUser(registered, "someip")

        val request = RandomProvider.constructAuthRequest(hwid = registered.hwid)

        Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/auth/register")
        } Then {
            statusCode(409)
        }
    }

    @Test
    fun `should login valid user`() {
        val request = RandomProvider.constructAuthRequest()
        authenticationService.registerUser(request, "someip")

        val token = Given {
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/auth/login")
        } Then {
            statusCode(200)
            body("token", notNullValue())
        } Extract {
            path<String>("token")
        }

        assertTrue(authUtils.validateJwtToken(token))
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
            statusCode(404)
        }
    }

    @Test
    fun `shouldn't login user with invalid password`() {
        val registerRequest = RandomProvider.constructAuthRequest()
        authenticationService.registerUser(registerRequest, "someip")

        val loginRequest = RandomProvider.constructAuthRequest(username = registerRequest.username)

        Given {
            contentType(ContentType.JSON)
            body(loginRequest)
        } When {
            post("/auth/login")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `shouldn't login user with invalid hwid`() {
        val registerRequest = RandomProvider.constructAuthRequest()
        authenticationService.registerUser(registerRequest, "someip")

        val loginRequest = RandomProvider.constructAuthRequest(
            username = registerRequest.username,
            password = registerRequest.password
        )
        println(loginRequest)
        Given {
            contentType(ContentType.JSON)
            body(loginRequest)
        } When {
            post("/auth/login")
        } Then {
            statusCode(401)
        }
    }

    @ParameterizedTest
    @MethodSource("com.intezya.abysscore.utils.providers.UserProvider#provideUsernameWithAnyCases")
    fun `should login user with any username case`(original: String, target: String) {
        val registerRequest = RandomProvider.constructAuthRequest(username = original)
        authenticationService.registerUser(registerRequest, "someip")

        val loginRequest = RandomProvider.constructAuthRequest(
            username = target,
            password = registerRequest.password,
            hwid = registerRequest.hwid
        )

        Given {
            contentType(ContentType.JSON)
            body(loginRequest)
        } When {
            post("/auth/login")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `should get user info by token`() {
        val user = RandomProvider.constructUser(id = 1L)
        val token = authUtils.generateJwtToken(user)

        Given {
            header("Authorization", "Bearer $token")
        } When {
            get("/auth/info")
        } Then {
            statusCode(200)
            contentType(ContentType.JSON)
            body("id", equalTo(user.id?.toInt()))
            body("username", equalTo(user.username))
            body("hwid", equalTo(user.hwid))
            body("access_level", equalTo(-1))
        }
    }
}

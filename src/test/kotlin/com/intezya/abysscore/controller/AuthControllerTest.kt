package com.intezya.abysscore.controller

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.admin.AdminAuthRequest
import com.intezya.abysscore.model.entity.Admin
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.AdminRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.jwt.JwtUtils
import com.intezya.abysscore.security.password.PasswordUtils
import com.intezya.abysscore.security.service.AuthenticationService
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
class AuthControllerTest {
    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Autowired
    private lateinit var passwordUtils: PasswordUtils

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
    }

    @AfterEach
    fun cleanUp() {
        adminRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `context loads`() {
        assertTrue(true)
    }

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
                statusCode(200)
                body("token", notNullValue())
            } Extract {
                path<String>("token")
            }

        assertTrue(jwtUtils.validateJwtToken(token))
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

        val token =
            Given {
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

        assertTrue(jwtUtils.validateJwtToken(token))
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

        val loginRequest =
            RandomProvider.constructAuthRequest(
                username = registerRequest.username,
                password = registerRequest.password,
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
    fun `should login user with any username case`(
        original: String,
        target: String,
    ) {
        val registerRequest = RandomProvider.constructAuthRequest(username = original)
        authenticationService.registerUser(registerRequest, "someip")

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
            statusCode(200)
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
                statusCode(200)
                body("token", notNullValue())
            } Extract {
                path<String>("token")
            }

        val authInfo = jwtUtils.getUserInfoFromToken(token)

        assertNotNull(authInfo.hwid)
        println(authInfo)
        assertEquals(passwordUtils.hashHwid(loginRequest.hwid), authInfo.hwid)
    }

    @Test
    fun `should get user info by token`() {
        val user = RandomProvider.constructUser(id = 1L)
        val token = jwtUtils.generateJwtToken(user)

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

    @Test
    @Transactional
    fun `should login as admin`() {
        val user = RandomProvider.constructUser()
        userRepository.save(user)
        val freshUser = userRepository.findById(user.id!!).orElseThrow()
        val admin = Admin(user = freshUser, telegramId = 123456789L, accessLevel = AccessLevel.DEV)
        adminRepository.save(admin)

        authenticationService.adminLogin(
            AdminAuthRequest(
                username = user.username,
                password = user.password,
                hwid = user.hwid!!,
            ),
            "someip",
        )
    }
}

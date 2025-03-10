package com.intezya.abysscore.controller

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.constants.BEARER_PREFIX
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.GameItemRepository
import com.intezya.abysscore.repository.UserItemRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.service.AuthenticationService
import com.intezya.abysscore.security.utils.JwtUtils
import com.intezya.abysscore.security.utils.PasswordUtils
import io.restassured.RestAssured
import io.restassured.parsing.Parser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseApiTest {
    @Autowired
    protected lateinit var jwtUtils: JwtUtils

    @Autowired
    protected lateinit var authenticationService: AuthenticationService

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var gameItemRepository: GameItemRepository

    @Autowired
    protected lateinit var userItemRepository: UserItemRepository

    @Autowired
    protected lateinit var passwordUtils: PasswordUtils

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON

        userRepository.deleteAll()
        gameItemRepository.deleteAll()
        userItemRepository.deleteAll()
    }

    @AfterEach
    fun cleanUp() {
    }

    @Test
    fun `context loads`() {
        assertTrue(true)
    }

    protected fun createAuthorizationHeader(token: String): String = "$BEARER_PREFIX$token"

    protected fun generateToken(accessLevel: AccessLevel): String {
        val user =
            User(
                id = null,
                username = "username",
                password = "password",
                hwid = "hwid",
                accessLevel = accessLevel,
            )
        userRepository.save(user)
        return jwtUtils.generateJwtToken(user)
    }

    protected fun generateToken(): String = generateToken(accessLevel = AccessLevel.USER)
}

package com.intezya.abysscore.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.constants.BEARER_PREFIX
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.GameItemRepository
import com.intezya.abysscore.repository.UserItemRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.toAuthDTO
import com.intezya.abysscore.security.utils.JwtUtils
import com.intezya.abysscore.security.utils.PasswordUtils
import com.intezya.abysscore.service.UserService
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
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

typealias JwtToken = String

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseApiTest {
    @Autowired
    protected lateinit var jwtUtils: JwtUtils

    @Autowired
    protected lateinit var userService: UserService

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var gameItemRepository: GameItemRepository

    @Autowired
    protected lateinit var userItemRepository: UserItemRepository

    @Autowired
    protected lateinit var passwordUtils: PasswordUtils

    private val f: Faker = faker {}

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> jacksonObjectMapper() },
        )

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

    protected fun createAuthorizationHeader(token: JwtToken): String = "$BEARER_PREFIX$token"

    protected fun generateUserWithToken(accessLevel: AccessLevel = AccessLevel.USER): Pair<User, JwtToken> {
        val user =
            User(
                id = 0L,
                username = f.name.firstName(),
                password = passwordUtils.hashPassword("password"),
                hwid = passwordUtils.hashHwid(f.random.nextUUID()),
                accessLevel = accessLevel,
            )
        userRepository.save(user)
        return Pair(user, jwtUtils.generateToken(user.toAuthDTO()))
    }

    protected fun generateToken(accessLevel: AccessLevel): JwtToken = generateUserWithToken(accessLevel).second
    protected fun generateToken(): JwtToken = generateToken(accessLevel = AccessLevel.USER)
    protected fun generateToken(user: User): JwtToken = jwtUtils.generateToken(user.toAuthDTO())
}

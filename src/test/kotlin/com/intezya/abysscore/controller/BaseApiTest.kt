package com.intezya.abysscore.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.constants.BEARER_PREFIX
import com.intezya.abysscore.constants.MATCH_INVITES_ENDPOINT
import com.intezya.abysscore.constants.USER_PREFERENCES_INVITES_ENDPOINT
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserGlobalStatistic
import com.intezya.abysscore.repository.*
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.dto.toAuthDTO
import com.intezya.abysscore.security.utils.JwtUtils
import com.intezya.abysscore.security.utils.PasswordUtils
import com.intezya.abysscore.service.UserService
import com.intezya.abysscore.utils.providers.RandomProvider
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.parsing.Parser
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

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
    protected lateinit var matchRepository: MatchRepository

    @Autowired
    protected lateinit var gameItemRepository: GameItemRepository

    @Autowired
    protected lateinit var userItemRepository: UserItemRepository

    @Autowired
    protected lateinit var passwordUtils: PasswordUtils

    @Autowired
    private lateinit var userGlobalStatisticRepository: UserGlobalStatisticRepository

    private val f: Faker = faker {}

    @LocalServerPort
    private var port: Int = 0

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> jacksonObjectMapper() },
        )

        val transactionTemplate = TransactionTemplate(transactionManager)

        transactionTemplate.execute {
            entityManager.createQuery("UPDATE User u SET u.currentMatch = NULL").executeUpdate()
            entityManager.createQuery("UPDATE User u SET u.currentBadge = NULL").executeUpdate()

            matchRepository.deleteAll()
            userRepository.deleteAll()
            gameItemRepository.deleteAll()
            userItemRepository.deleteAll()
        }
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
        val statistic = UserGlobalStatistic().apply { this.user = user }
        userGlobalStatisticRepository.save(statistic)
        return Pair(user, jwtUtils.generateToken(user.toAuthDTO()))
    }

    protected fun generateToken(accessLevel: AccessLevel): JwtToken = generateUserWithToken(accessLevel).second
    protected fun generateToken(): JwtToken = generateToken(accessLevel = AccessLevel.USER)
    protected fun generateToken(user: User): JwtToken = jwtUtils.generateToken(user.toAuthDTO())

    protected fun jsonRequest() = Given {
        contentType(ContentType.JSON)
    }

    protected fun authenticatedRequest(token: String) = Given {
        header("Authorization", "Bearer $token")
        contentType(ContentType.JSON)
    }

    protected fun authenticatedRequest(accessLevel: AccessLevel = AccessLevel.USER) = authenticatedRequest(generateToken(accessLevel))

    protected fun createGameItem(): GameItem {
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        return gameItemRepository.save(gameItem)
    }

    protected fun createMultipleGameItems(count: Int): List<GameItem> = (1..count).map {
        gameItemRepository.save(RandomProvider.constructCreateGameItemRequest().toEntity().copy())
    }

    protected fun createUser(userRequest: AuthRequest = RandomProvider.constructAuthRequest()): AuthRequest {
        userService.create(userRequest)
        return userRequest
    }

    protected fun createUserInviteRequest(inviteeUsername: String) = CreateMatchInviteRequest(inviteeUsername = inviteeUsername)

    protected fun setAcceptInvites(token: String, accept: Boolean) {
        val request = UpdateMatchInvitesRequest(receiveMatchInvites = accept)

        authenticatedRequest(token)
            .body(request)
            .When {
                patch(USER_PREFERENCES_INVITES_ENDPOINT)
            }.Then {
                statusCode(HttpStatus.OK.value())
            }
    }

    protected fun createInvite(inviterToken: String, inviteeUsername: String): Long {
        val request = createUserInviteRequest(inviteeUsername)

        val response = authenticatedRequest(inviterToken)
            .body(request)
            .When {
                post(MATCH_INVITES_ENDPOINT)
            }.Then {
                statusCode(HttpStatus.CREATED.value())
            }.Extract {
                body().jsonPath().getMap<String, Any>("")
            }

        return (response["id"] as Int).toLong()
    }
}

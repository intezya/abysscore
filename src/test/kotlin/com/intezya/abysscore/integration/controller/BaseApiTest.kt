package com.intezya.abysscore.integration.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intezya.abysscore.constants.BEARER_PREFIX
import com.intezya.abysscore.constants.MATCH_INVITES_ENDPOINT
import com.intezya.abysscore.constants.USER_PREFERENCES_INVITES_ENDPOINT
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.model.dto.matchinvite.CreateMatchInviteRequest
import com.intezya.abysscore.model.dto.user.UpdateMatchInvitesRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserGlobalStatistic
import com.intezya.abysscore.repository.*
import com.intezya.abysscore.security.dto.AuthRequest
import com.intezya.abysscore.security.utils.JwtUtils
import com.intezya.abysscore.security.utils.PasswordUtils
import com.intezya.abysscore.service.MatchMakingService
import com.intezya.abysscore.service.UserService
import com.intezya.abysscore.utils.containers.TestPostgresConfiguration
import com.intezya.abysscore.utils.fixtures.UserFixtures
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationEventPublisher
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
    protected lateinit var matchMakingService: MatchMakingService

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
    protected lateinit var matchDraftRepository: MatchDraftRepository

    @Autowired
    protected lateinit var passwordUtils: PasswordUtils

    @Autowired
    private lateinit var userGlobalStatisticRepository: UserGlobalStatisticRepository

    @Autowired
    protected lateinit var eventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var draftActionRepository: DraftActionRepository

    private val f: Faker = faker {}

    @LocalServerPort
    private var port: Int = 0

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    protected var mainWebsocketUrl = ""

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.defaultParser = Parser.JSON
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> jacksonObjectMapper() },
        )

        mainWebsocketUrl = "ws://localhost:$port/hubs/main"

        val transactionTemplate = TransactionTemplate(transactionManager)

        transactionTemplate.execute {
            entityManager.createNativeQuery("TRUNCATE draft_actions CASCADE").executeUpdate()
            entityManager.createQuery("UPDATE User u SET u.currentMatch = NULL").executeUpdate()
            entityManager.createQuery("UPDATE User u SET u.currentBadge = NULL").executeUpdate()

            draftActionRepository.deleteAll()
            matchDraftRepository.deleteAll()
            matchRepository.deleteAll()
            userRepository.deleteAll()
            gameItemRepository.deleteAll()
            userItemRepository.deleteAll()
        }
    }

    @Test
    fun `context loads`() {
        Assertions.assertTrue(true)
    }

    protected fun createAuthorizationHeader(token: JwtToken): String = "$BEARER_PREFIX$token"

    // TODO: return dto with user and token fields
    protected fun generateUserWithToken(accessLevel: AccessLevel = AccessLevel.USER): Pair<User, JwtToken> {
        val user = UserFixtures.generateDefaultUserWithRandomCreds().apply {
            this.accessLevel = accessLevel
        }
        userRepository.save(user)
        val statistic = UserGlobalStatistic().apply { this.user = user }
        user.globalStatistic = statistic
        userGlobalStatisticRepository.save(statistic)
        return Pair(user, jwtUtils.generateToken(user))
    }

    protected fun generateToken(accessLevel: AccessLevel): JwtToken = generateUserWithToken(accessLevel).second
    protected fun generateToken(): JwtToken = generateToken(accessLevel = AccessLevel.USER)
    protected fun generateToken(user: User): JwtToken = jwtUtils.generateToken(user)

    protected fun jsonRequest() = Given {
        contentType(ContentType.JSON)
    }

    protected fun authenticatedRequest(token: String) = Given {
        header("Authorization", "Bearer $token")
        contentType(ContentType.JSON)
    }

    protected fun authenticatedRequest(accessLevel: AccessLevel = AccessLevel.USER) =
        authenticatedRequest(generateToken(accessLevel))

    protected fun createGameItem(): GameItem {
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        return gameItemRepository.save(gameItem)
    }

    protected fun createMultipleGameItems(count: Int): List<GameItem> = (1..count).map {
        gameItemRepository.save(RandomProvider.constructCreateGameItemRequest().toEntity())
    }

    protected fun createUser(userRequest: AuthRequest = RandomProvider.constructAuthRequest()): AuthRequest {
        userService.create(userRequest)
        return userRequest
    }

    protected fun createUserInviteRequest(inviteeUsername: String) =
        CreateMatchInviteRequest(inviteeUsername = inviteeUsername)

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

    protected fun createMatch(user1: User, user2: User, withStatus: MatchStatus = MatchStatus.PENDING): Match {
        val match = Match().apply {
            this.player1 = user1
            this.player2 = user2
        }
        matchRepository.save(match)
        return match
    }

    protected fun createMatch(withStatus: MatchStatus = MatchStatus.PENDING): CreateMatchResult {
        val (user1, _) = generateUserWithToken()
        val (user2, _) = generateUserWithToken()

        val match = matchMakingService.createMatch(user1, user2)

        match.apply { this.status = withStatus }

        matchRepository.saveAndFlush(match)

        user1.apply { this.currentMatch = match }
        user2.apply { this.currentMatch = match }

        userRepository.save(user1)
        userRepository.save(user2)

        return CreateMatchResult(user1, user2, match)
    }

    data class CreateMatchResult(val player1: User, val player2: User, val match: Match)
}

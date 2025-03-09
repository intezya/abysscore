package com.intezya.abysscore.controller

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.GameItemRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.jwt.JwtUtils
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
class GameItemControllerTest {
    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var gameItemRepository: GameItemRepository

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
        userRepository.deleteAll()
        gameItemRepository.deleteAll()
    }

    @Test
    fun `context loads`() {
        assertTrue(true)
    }

    @Test
    fun `should create game item`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest()

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(201)
            body("id", notNullValue())
        }
    }

    @Test
    fun `shouldn't create game item with blank name`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest(name = "")

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `shouldn't create game item with blank collection`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest(collection = "")

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `shouldn't create game item with invalid type`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest(type = -1)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `shouldn't create game item with invalid rarity`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest(rarity = -1)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `shouldn't create game item without required level`() {
        val token = generateToken()
        val request = RandomProvider.constructCreateGameItemRequest()

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            post("/items")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `get all should return empty list`() {
        val token = generateToken(AccessLevel.DEV)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
        } When {
            get("/items")
        } Then {
            statusCode(200)
            body("content", notNullValue())
        }
    }

    @Test
    fun `get all should return list of items`() {
        val token = generateToken()
        val request = RandomProvider.constructCreateGameItemRequest().toEntity()
        val n = 100
        for (i in 1..n) {
            gameItemRepository.save(request.copy())
        }

        val response =
            Given {
                header("Authorization", "Bearer $token")
                contentType(ContentType.JSON)
            } When {
                get("/items")
            } Then {
                statusCode(200)
                body("content", notNullValue())
            } Extract {
                response().jsonPath()
            }

        val content = response.getList<Map<String, Any>>("content")
        val page = response.getMap<String, String>("page")

        assertEquals(page["size"], content.size)
        assertEquals(n, page["total_elements"])
    }

    @Test
    fun `get all shouldn't work without authorization`() {
        Given {
            contentType(ContentType.JSON)
        } When {
            get("/items")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `get one should return item`() {
        val token = generateToken()
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        gameItemRepository.save(gameItem)

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
        } When {
            get("/items/${gameItem.id}")
        } Then {
            statusCode(200)
            body("id", equalTo(gameItem.id?.toInt()))
            body("name", equalTo(gameItem.name))
            body("collection", equalTo(gameItem.collection))
            body("type", equalTo(gameItem.type))
            body("rarity", equalTo(gameItem.rarity))
        }
    }

    @Test
    fun `get one shouldn't return item that not exist`() {
        val token = generateToken()

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
        } When {
            get("/items/${1}")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `get one shouldn't work without authorization`() {
        Given {
            contentType(ContentType.JSON)
        } When {
            get("/items/${1}")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `put should work`() {
        val token = generateToken(AccessLevel.DEV)
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        gameItemRepository.save(gameItem)

        val request = RandomProvider.constructCreateGameItemRequest()

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            put("/items/${gameItem.id}")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `put shouldn't work if item doesn't exist`() {
        val token = generateToken(AccessLevel.DEV)
        val request = RandomProvider.constructCreateGameItemRequest()

        Given {
            header("Authorization", "Bearer $token")
            contentType(ContentType.JSON)
            body(request)
        } When {
            put("/items/1")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `put shouldn't work without authorization`() {
        Given {
            contentType(ContentType.JSON)
        } When {
            put("/items/${1}")
        } Then {
            statusCode(401)
        }
    }

    @Test
    fun `delete should work`() {
        val token = generateToken(AccessLevel.DEV)
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        gameItemRepository.save(gameItem)

        Given {
            header("Authorization", "Bearer $token")
        } When {
            delete("/items/${gameItem.id}")
        } Then {
            statusCode(204)
        }
    }

    @Test
    fun `delete shouldn't work if not found`() {
        val token = generateToken(AccessLevel.DEV)

        Given {
            header("Authorization", "Bearer $token")
        } When {
            delete("/items/1")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `delete shouldn't work without required level`() {
        val token = generateToken(AccessLevel.USER)
        val gameItem = RandomProvider.constructCreateGameItemRequest().toEntity()
        gameItemRepository.save(gameItem)

        Given {
            header("Authorization", "Bearer $token")
        } When {
            delete("/items/${gameItem.id}")
        } Then {
            statusCode(403)
        }
    }

    private fun generateToken(accessLevel: AccessLevel): String {
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

    private fun generateToken(): String = generateToken(accessLevel = AccessLevel.USER)
}

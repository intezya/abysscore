package com.intezya.abysscore.integration.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.integration.BaseApiTest
import com.intezya.abysscore.utils.providers.RandomProvider
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.springframework.http.HttpStatus
import kotlin.test.Test

class GameItemControllerIntegratonTest : BaseApiTest() {
    @Nested
    inner class GameItemCreate {
        @Test
        fun `should create game item`() {
            val request = RandomProvider.constructCreateGameItemRequest()

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(201)
                    body("id", CoreMatchers.notNullValue())
                }
        }

        @Test
        fun `shouldn't create game item with blank name`() {
            val request = RandomProvider.constructCreateGameItemRequest(name = "")

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(400)
                }
        }

        @Test
        fun `shouldn't create game item with blank collection`() {
            val request = RandomProvider.constructCreateGameItemRequest(collection = "")

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(400)
                }
        }

        @Test
        fun `shouldn't create game item with invalid type`() {
            val request = RandomProvider.constructCreateGameItemRequest(type = -1)

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        @Test
        fun `shouldn't create game item with invalid rarity`() {
            val request = RandomProvider.constructCreateGameItemRequest(rarity = -1)

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(HttpStatus.BAD_REQUEST.value())
                }
        }

        @Test
        fun `shouldn't create game item without required level`() {
            val request = RandomProvider.constructCreateGameItemRequest()

            authenticatedRequest()
                .body(request)
                .When {
                    post("/items")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }
    }

    @Nested
    inner class GameItemGetAll {
        @Test
        fun `get all should return empty list`() {
            authenticatedRequest(AccessLevel.DEV)
                .When {
                    get("/items")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content", CoreMatchers.notNullValue())
                }
        }

        @Test
        fun `get all should return list of items`() {
            val n = 100
            createMultipleGameItems(n)

            val response = authenticatedRequest()
                .When {
                    get("/items")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content", CoreMatchers.notNullValue())
                }.Extract {
                    response().jsonPath()
                }

            val content = response.getList<Map<String, Any>>("content")
            val page = response.getMap<String, String>("page")

            Assertions.assertEquals(page["size"], content.size)
            Assertions.assertEquals(n, page["total_elements"])
        }

        @Test
        fun `get all shouldn't work without authorization`() {
            Given {
                contentType(ContentType.JSON)
            }.When {
                get("/items")
            }.Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }
    }

    @Nested
    inner class GameItemGetOne {
        @Test
        fun `get one should return item`() {
            val gameItem = createGameItem()

            authenticatedRequest()
                .When {
                    get("/items/${gameItem.id}")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("id", CoreMatchers.equalTo(gameItem.id.toInt()))
                    body("name", CoreMatchers.equalTo(gameItem.name))
                    body("collection", CoreMatchers.equalTo(gameItem.collection))
                    body("type", CoreMatchers.equalTo(gameItem.type))
                    body("rarity", CoreMatchers.equalTo(gameItem.rarity))
                }
        }

        @Test
        fun `get one shouldn't return item that not exist`() {
            authenticatedRequest()
                .When {
                    get("/items/1")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `get one shouldn't work without authorization`() {
            Given {
                contentType(ContentType.JSON)
            }.When {
                get("/items/1")
            }.Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }
    }

    @Nested
    inner class GameItemPut {
        @Test
        fun `put should work`() {
            val gameItem = createGameItem()
            val request = RandomProvider.constructCreateGameItemRequest()

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    put("/items/${gameItem.id}")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                }
        }

        @Test
        fun `put shouldn't work if item doesn't exist`() {
            val request = RandomProvider.constructCreateGameItemRequest()

            authenticatedRequest(AccessLevel.DEV)
                .body(request)
                .When {
                    put("/items/1")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `put shouldn't work without authorization`() {
            Given {
                contentType(ContentType.JSON)
            }.When {
                put("/items/1")
            }.Then {
                statusCode(HttpStatus.UNAUTHORIZED.value())
            }
        }
    }

    @Nested
    inner class GameItemDelete {
        @Test
        fun `delete should work`() {
            val gameItem = createGameItem()

            authenticatedRequest(AccessLevel.DEV)
                .When {
                    delete("/items/${gameItem.id}")
                }.Then {
                    statusCode(HttpStatus.NO_CONTENT.value())
                }
        }

        @Test
        fun `delete shouldn't work if not found`() {
            authenticatedRequest(AccessLevel.DEV)
                .When {
                    delete("/items/1")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `delete shouldn't work without required level`() {
            val gameItem = createGameItem()

            authenticatedRequest(AccessLevel.USER)
                .When {
                    delete("/items/${gameItem.id}")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }
    }
}

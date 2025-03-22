package com.intezya.abysscore.integrationTest

import com.intezya.abysscore.constants.ACCOUNT_INVENTORY_ENDPOINT
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Nested
import org.springframework.http.HttpStatus
import kotlin.test.Test

class UserItemControllerTest : BaseApiTest() {
    @Nested
    inner class GetSelfInventory {
        @Test
        fun `should return user's own inventory`() {
            val (user, token) = generateUserWithToken()
            val gameItem = createGameItem()
            createUserItem(user, gameItem)

            authenticatedRequest(token)
                .When {
                    get(ACCOUNT_INVENTORY_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content.size()", equalTo(1))
                    body("content[0].game_item.id", equalTo(gameItem.id.toInt()))
                }
        }

        @Test
        fun `should return empty inventory for user with no items`() {
            val (_, token) = generateUserWithToken()

            authenticatedRequest(token)
                .When {
                    get(ACCOUNT_INVENTORY_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content.size()", equalTo(0))
                }
        }

        @Test
        fun `should return paginated results for large inventory`() {
            val (user, token) = generateUserWithToken()
            val gameItems = List(25) { createGameItem() }
            gameItems.forEach { createUserItem(user, it) }

            authenticatedRequest(token)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .When {
                    get(ACCOUNT_INVENTORY_ENDPOINT)
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content.size()", equalTo(10))
                    body("page.total_elements", equalTo(25))
                }
        }
    }

    @Nested
    inner class GetUserInventory {
        @Test
        fun `should return other user's inventory when caller has VIEW_INVENTORY access`() {
            val (admin, adminToken) = generateUserWithToken(AccessLevel.VIEW_INVENTORY)
            val (user, _) = generateUserWithToken()
            val gameItem = createGameItem()
            createUserItem(user, gameItem)

            authenticatedRequest(adminToken)
                .When {
                    get("$ACCOUNT_INVENTORY_ENDPOINT/${user.id}")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("content.size()", equalTo(1))
                    body("content[0].game_item.id", equalTo(gameItem.id.toInt()))
                }
        }

        @Test
        fun `should not allow access to other user's inventory without VIEW_INVENTORY permission`() {
            val (_, regularUserToken) = generateUserWithToken(AccessLevel.USER)
            val (user, _) = generateUserWithToken()

            authenticatedRequest(regularUserToken)
                .When {
                    get("$ACCOUNT_INVENTORY_ENDPOINT/${user.id}")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }
    }

    @Nested
    inner class CreateUserItem {
        @Test
        fun `should create item for user when caller has GIVE_ITEM access`() {
            val (admin, adminToken) = generateUserWithToken(AccessLevel.GIVE_ITEM)
            val (user, _) = generateUserWithToken()
            val gameItem = createGameItem()

            authenticatedRequest(adminToken)
                .queryParam("item_id", gameItem.id)
                .When {
                    post("$ACCOUNT_INVENTORY_ENDPOINT/${user.id}")
                }.Then {
                    statusCode(HttpStatus.OK.value())
                    body("game_item.id", equalTo(gameItem.id.toInt()))
                }
        }

        @Test
        fun `should not allow creating item for user without GIVE_ITEM permission`() {
            val (_, regularUserToken) = generateUserWithToken(AccessLevel.USER)
            val (user, _) = generateUserWithToken()
            val gameItem = createGameItem()

            authenticatedRequest(regularUserToken)
                .queryParam("item_id", gameItem.id)
                .When {
                    post("$ACCOUNT_INVENTORY_ENDPOINT/${user.id}")
                }.Then {
                    statusCode(HttpStatus.FORBIDDEN.value())
                }
        }

        @Test
        fun `should return NOT_FOUND when trying to create item with non-existent game item id`() {
            val (admin, adminToken) = generateUserWithToken(AccessLevel.GIVE_ITEM)
            val (user, _) = generateUserWithToken()
            val nonExistentItemId = 9999L

            authenticatedRequest(adminToken)
                .queryParam("item_id", nonExistentItemId)
                .When {
                    post("$ACCOUNT_INVENTORY_ENDPOINT/${user.id}")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }

        @Test
        fun `should return NOT_FOUND when trying to create item for non-existent user`() {
            val (admin, adminToken) = generateUserWithToken(AccessLevel.GIVE_ITEM)
            val gameItem = createGameItem()
            val nonExistentUserId = 9999L

            authenticatedRequest(adminToken)
                .queryParam("item_id", gameItem.id)
                .When {
                    post("$ACCOUNT_INVENTORY_ENDPOINT/$nonExistentUserId")
                }.Then {
                    statusCode(HttpStatus.NOT_FOUND.value())
                }
        }
    }

    private fun createUserItem(user: User, gameItem: GameItem): UserItem {
        val userItem = UserItem().apply {
            this.user = user
            this.gameItem = gameItem
        }
        return userItemRepository.save(userItem)
    }
}

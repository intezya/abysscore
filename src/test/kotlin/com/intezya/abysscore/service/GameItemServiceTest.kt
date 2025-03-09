package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.gameitem.CreateGameItemRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.repository.GameItemRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.Test

class GameItemServiceTest {
    @MockK
    private lateinit var gameItemRepository: GameItemRepository

    @InjectMockKs
    private lateinit var gameItemService: GameItemService

    private val gameItem =
        GameItem(
            id = 1L,
            name = "test",
            collection = "test",
            type = 1,
            rarity = 1,
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { gameItemRepository.save(any()) } returns gameItem
    }

    @Test
    fun `create item successfully`() {
        val testCreateItemRequest =
            CreateGameItemRequest(
                name = "test",
                collection = "test",
                type = 1,
                rarity = 1,
            )

        val result = gameItemService.createGameItem(testCreateItemRequest)

        verify { gameItemRepository.save(any()) }

        assertEquals(1L, result.id)
        assertEquals(testCreateItemRequest.name, result.name)
        assertEquals(testCreateItemRequest.collection, result.collection)
        assertEquals(testCreateItemRequest.type, result.type)
        assertEquals(testCreateItemRequest.rarity, result.rarity)
    }

    @Test
    fun `find item successfully`() {
        every { gameItemRepository.findById(any()) } returns Optional.of(gameItem)

        val result = gameItemService.findById(gameItem.id ?: 1L)

        verify {
            gameItemRepository.findById(any())
        }

        assertEquals(gameItem.id, result.id)
        assertEquals(gameItem.name, result.name)
        assertEquals(gameItem.collection, result.collection)
        assertEquals(gameItem.type, result.type)
        assertEquals(gameItem.rarity, result.rarity)
    }

    @Test
    fun `find item not found`() {
        every { gameItemRepository.findById(any()) } returns Optional.empty()

        val responseEx =
            assertThrows<ResponseStatusException> {
                gameItemService.findById(gameItem.id ?: 1L)
            }

        assertEquals(HttpStatus.NOT_FOUND, responseEx.statusCode)
        assertEquals("Item not found", responseEx.reason)
    }

    @Test
    fun `find all successfully`() {
        val pageable = PageRequest.of(0, 10)
        val gameItemsPage: Page<GameItem> = PageImpl(listOf(gameItem))

        every { gameItemRepository.findAll(pageable) } returns gameItemsPage

        val result = gameItemService.findAll(pageable)

        verify { gameItemRepository.findAll(pageable) }

        assertEquals(1, result.content.size)
        assertEquals(gameItem, result.content.first())
    }

    @Test
    fun `update item successfully`() {
        val itemId = 1L
        val createGameItemRequest =
            CreateGameItemRequest(
                name = "Updated Item",
                collection = "Updated Collection",
                type = 2,
                rarity = 3,
            )

        every { gameItemRepository.existsById(itemId) } returns true
        every { gameItemRepository.save(any()) } returns
            gameItem.copy(
                name = createGameItemRequest.name,
                collection = createGameItemRequest.collection,
                type = createGameItemRequest.type,
                rarity = createGameItemRequest.rarity,
            )

        val result = gameItemService.updateItem(itemId, createGameItemRequest)

        verify { gameItemRepository.existsById(itemId) }
        verify { gameItemRepository.save(any()) }

        assertEquals(itemId, result.id)
        assertEquals(createGameItemRequest.name, result.name)
        assertEquals(createGameItemRequest.collection, result.collection)
        assertEquals(createGameItemRequest.type, result.type)
        assertEquals(createGameItemRequest.rarity, result.rarity)
    }

    @Test
    fun `update item not found`() {
        val itemId = 1L
        val createGameItemRequest =
            CreateGameItemRequest(
                name = "Updated Item",
                collection = "Updated Collection",
                type = 2,
                rarity = 3,
            )

        every { gameItemRepository.existsById(itemId) } returns false

        val responseEx =
            assertThrows<ResponseStatusException> {
                gameItemService.updateItem(itemId, createGameItemRequest)
            }

        assertEquals(HttpStatus.NOT_FOUND, responseEx.statusCode)
        assertEquals("Item not found", responseEx.reason)
    }

    @Test
    fun `delete item successfully`() {
        val itemId = 1L

        every { gameItemRepository.existsById(itemId) } returns true
        every { gameItemRepository.deleteById(itemId) } just Runs

        gameItemService.deleteItem(itemId)

        verify { gameItemRepository.existsById(itemId) }
        verify { gameItemRepository.deleteById(itemId) }
    }

    @Test
    fun `delete item not found`() {
        val itemId = 1L

        every { gameItemRepository.existsById(itemId) } returns false

        val responseEx =
            assertThrows<ResponseStatusException> {
                gameItemService.deleteItem(itemId)
            }

        assertEquals(HttpStatus.NOT_FOUND, responseEx.statusCode)
        assertEquals("Item not found", responseEx.reason)
    }
}

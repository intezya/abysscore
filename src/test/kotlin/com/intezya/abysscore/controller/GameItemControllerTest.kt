package com.intezya.abysscore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.dto.gameitem.CreateGameItemRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.service.GameItemService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException

class GameItemControllerTest {
    @MockK
    private lateinit var gameItemService: GameItemService

    @InjectMockKs
    private lateinit var gameItemController: GameItemController

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    private val testItemId = 1L
    private val testItemName = "Test Item"
    private val testCollection = "Test Collection"
    private val testType = 1
    private val testRarity = 3

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockMvc = MockMvcBuilders.standaloneSetup(gameItemController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `createItem should return created game item`() {
        // Given
        val createRequest = CreateGameItemRequest(testItemName, testCollection, testType, testRarity)
        val createdItem = GameItem(testItemId, testItemName, testCollection, testType, testRarity)
        val requestSlot = slot<CreateGameItemRequest>()

        every { gameItemService.createGameItem(capture(requestSlot)) } returns createdItem

        // When/Then
        mockMvc
            .perform(
                post("/items/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(testItemId))
            .andExpect(jsonPath("$.name").value(testItemName))
            .andExpect(jsonPath("$.collection").value(testCollection))
            .andExpect(jsonPath("$.type").value(testType))
            .andExpect(jsonPath("$.rarity").value(testRarity))

        // Verify captured request
        assert(requestSlot.captured.name == testItemName)
        assert(requestSlot.captured.collection == testCollection)
        assert(requestSlot.captured.type == testType)
        assert(requestSlot.captured.rarity == testRarity)

        verify { gameItemService.createGameItem(any()) }
    }

//    @Test
//    fun `getAll should return paged list of game items`() {
//        // Given
//        val pageable = PageRequest.of(0, 10)
//        val testItems = listOf(GameItem(testItemId, testItemName, testCollection, testType, testRarity))
//        val page = PageImpl(testItems, pageable, testItems.size.toLong())
//
//        every { gameItemService.findAll(any()) } returns page
//
//        // When/Then
//        mockMvc.perform(get("/items"))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.content[0].id").value(testItemId))
//            .andExpect(jsonPath("$.content[0].name").value(testItemName))
//            .andExpect(jsonPath("$.totalElements").value(testItems.size))
//
//        verify { gameItemService.findAll(any()) }
//    }

    @Test
    fun `getOne should return single game item`() {
        // Given
        val testItem = GameItem(testItemId, testItemName, testCollection, testType, testRarity)

        every { gameItemService.findById(testItemId) } returns testItem

        // When/Then
        mockMvc
            .perform(get("/items/$testItemId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testItemId))
            .andExpect(jsonPath("$.name").value(testItemName))
            .andExpect(jsonPath("$.collection").value(testCollection))
            .andExpect(jsonPath("$.type").value(testType))
            .andExpect(jsonPath("$.rarity").value(testRarity))

        verify { gameItemService.findById(testItemId) }
    }

    @Test
    fun `getOne should handle not found exception`() {
        // Given
        every { gameItemService.findById(testItemId) } throws
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Item not found",
            )

        // When/Then
        mockMvc
            .perform(get("/items/$testItemId"))
            .andExpect(status().isNotFound)

        verify { gameItemService.findById(testItemId) }
    }

    @Test
    fun `updateItem should return updated game item`() {
        // Given
        val updateRequest = CreateGameItemRequest("Updated Item", "Updated Collection", 2, 4)
        val updatedItem = GameItem(testItemId, "Updated Item", "Updated Collection", 2, 4)
        val requestSlot = slot<CreateGameItemRequest>()

        every { gameItemService.updateItem(testItemId, capture(requestSlot)) } returns updatedItem

        // When/Then
        mockMvc
            .perform(
                put("/items/$testItemId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testItemId))
            .andExpect(jsonPath("$.name").value("Updated Item"))
            .andExpect(jsonPath("$.collection").value("Updated Collection"))
            .andExpect(jsonPath("$.type").value(2))
            .andExpect(jsonPath("$.rarity").value(4))

        // Verify captured request
        assert(requestSlot.captured.name == "Updated Item")
        assert(requestSlot.captured.collection == "Updated Collection")
        assert(requestSlot.captured.type == 2)
        assert(requestSlot.captured.rarity == 4)

        verify { gameItemService.updateItem(testItemId, any()) }
    }

    @Test
    fun `deleteItem should return no content status`() {
        // Given
        justRun { gameItemService.deleteItem(testItemId) }

        // When/Then
        mockMvc
            .perform(delete("/items/$testItemId"))
            .andExpect(status().isNoContent)

        verify { gameItemService.deleteItem(testItemId) }
    }
}

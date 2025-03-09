package com.intezya.abysscore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import com.intezya.abysscore.service.UserItemService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

class UserItemControllerTest {
    @MockK
    private lateinit var userItemService: UserItemService

    @InjectMockKs
    private lateinit var userItemController: UserItemController

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    private val testUserId = 1L
    private val testUsername = "testUser"
    private val testItemId = 2L
    private val testUserItemId = 3L
    private val testAccessLevel = 10
    private val testHwid = "test-hwid-123"
    private val testGameItem = GameItem(testItemId, "Test Item", "Test Collection", 1, 3)
    private val testSourceType = ItemSourceType.ADMIN

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockMvc = MockMvcBuilders.standaloneSetup(userItemController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `getAll should return user items for authenticated user`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val userAuthData = UserAuthInfoDTO(testUserId, testUsername, testHwid, testAccessLevel)
        val testUserItemDTO =
            UserItemDTO(
                testUserItemId,
                testGameItem,
                null,
                testSourceType,
                LocalDateTime.now(),
            )
        val page: Page<UserItemDTO> = PageImpl(listOf(testUserItemDTO), pageable, 1)
        val pageableSlot = slot<Pageable>()

        every { userItemService.findAllUserItems(testUserId, capture(pageableSlot)) } returns page

        // When/Then
        val result = userItemController.getAll(pageable, userAuthData)

        // Verify result
        assert(result.content.size == 1)
        assert(result.content[0].id == testUserItemDTO.id)
        assert(result.content[0].gameItem == testGameItem)

        // Verify service call
        verify { userItemService.findAllUserItems(testUserId, any()) }
    }

    @Test
    fun `getUserInventory should return user items for specified user`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val testUserItemDTO =
            UserItemDTO(
                testUserItemId,
                testGameItem,
                null,
                testSourceType,
                LocalDateTime.now(),
            )
        val page: Page<UserItemDTO> = PageImpl(listOf(testUserItemDTO), pageable, 1)
        val pageableSlot = slot<Pageable>()

        every { userItemService.findAllUserItems(testUserId, capture(pageableSlot)) } returns page

        // When/Then
        val result = userItemController.getUserInventory(pageable, testUserId)

        // Verify result
        assert(result.content.size == 1)
        assert(result.content[0].id == testUserItemDTO.id)
        assert(result.content[0].gameItem == testGameItem)

        // Verify service call
        verify { userItemService.findAllUserItems(testUserId, any()) }
    }

    @Test
    fun `create should issue item to player from admin`() {
        // Given
        val userAuthData = UserAuthInfoDTO(testUserId, testUsername, testHwid, testAccessLevel)
        val testUserItemDTO =
            UserItemDTO(
                testUserItemId,
                testGameItem,
                null,
                testSourceType,
                LocalDateTime.now(),
            )

        every {
            userItemService.issueForPlayerFromAdmin(testUsername, testItemId, testUserId)
        } returns testUserItemDTO

        // When/Then
        val result = userItemController.create(testUsername, testItemId, userAuthData)

        // Verify result
        assert(result.id == testUserItemDTO.id)
        assert(result.gameItem == testGameItem)
        assert(result.sourceType == testSourceType)

        // Verify service call
        verify { userItemService.issueForPlayerFromAdmin(testUsername, testItemId, testUserId) }
    }

    @Test
    fun `create should handle user not found exception`() {
        // Given
        val userAuthData = UserAuthInfoDTO(testUserId, testUsername, testHwid, testAccessLevel)

        every {
            userItemService.issueForPlayerFromAdmin(testUsername, testItemId, testUserId)
        } throws ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        // This would be better tested with MockMvc for HTTP status verification
        // For now, we'll just verify the exception is thrown

        try {
            userItemController.create(testUsername, testItemId, userAuthData)
            assert(false) { "Should have thrown exception" }
        } catch (e: ResponseStatusException) {
            assert(e.statusCode == HttpStatus.NOT_FOUND)
            assert(e.reason == "User not found")
        }

        verify { userItemService.issueForPlayerFromAdmin(testUsername, testItemId, testUserId) }
    }
}

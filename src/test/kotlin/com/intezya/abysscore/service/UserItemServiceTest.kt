package com.intezya.abysscore.service

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.model.dto.event.ItemIssueEvent
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import com.intezya.abysscore.repository.UserItemRepository
import com.intezya.abysscore.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import io.mockk.verify as verifyMock

class UserItemServiceTest {
    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var gameItemService: GameItemService

    @MockK
    private lateinit var userItemRepository: UserItemRepository

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var userItemService: UserItemService

    private val testUserId = 1L
    private val testUsername = "testUser"
    private val testAdminId = 2L
    private val testAdminUsername = "testAdmin"
    private val testItemId = 3L
    private val testPageable = PageRequest.of(0, 10)

    private lateinit var testUser: User
    private lateinit var testAdmin: User
    private lateinit var testGameItem: GameItem
    private lateinit var testUserItem: UserItem

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testUser = User(id = testUserId, username = testUsername, password = "hashedPassword", hwid = "hashedHwid")
        testAdmin =
            User(
                id = testAdminId,
                username = testAdminUsername,
                password = "hashedPassword",
                hwid = "hashedHwid",
                accessLevel = AccessLevel.DEV,
            )
        testGameItem =
            GameItem(id = testItemId, name = "Test Item", collection = "Test Collection", type = 1, rarity = 3)
        testUserItem =
            UserItem(
                id = 1L,
                user = testUser,
                gameItem = testGameItem,
                sourceType = ItemSourceType.ADMIN,
                createdAt = LocalDateTime.now(),
            )

        every { userService.findUserWithThrow(testUserId) } returns testUser
        every { userService.findUserWithThrow(testUsername) } returns testUser
        every { userService.findUserWithThrow(testAdminId) } returns testAdmin
        every { gameItemService.findById(testItemId) } returns testGameItem
        every { userItemRepository.save(any()) } returns testUserItem
        every { eventPublisher.sendActionEvent(any(), any(), any()) } just runs
    }

    @Test
    fun `findAllUserItems should return page of user items when user exists`() {
        val userItemsPage = PageImpl(listOf(testUserItem))
        every { userItemRepository.findByUserId(testUserId, testPageable) } returns userItemsPage

        val result = userItemService.findAllUserItems(testUserId, testPageable)

        assert(result.content.size == 1)
        assert(result.content[0].id == testUserItem.id)
        assert(result.content[0].gameItem == testGameItem)
        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock { userItemRepository.findByUserId(testUserId, testPageable) }
    }

    @Test
    fun `findAllUserItems should throw exception when user does not exist`() {
        every { userService.findUserWithThrow(testUserId) } throws
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found",
            )

        assertThrows<ResponseStatusException> {
            userItemService.findAllUserItems(testUserId, testPageable)
        }

        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock(exactly = 0) { userItemRepository.findByUserId(any(), any()) }
    }

    @Test
    fun `issueForPlayerFromAdmin should issue item and return DTO when all entities exist`() {
        val eventSlot = slot<ItemIssueEvent>()

        val result = userItemService.issueForPlayerFromAdmin(testUserId, testItemId, testAdminId)

        assert(result.gameItem == testGameItem)
        assert(result.sourceType == ItemSourceType.ADMIN)

        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock { gameItemService.findById(testItemId) }
        verifyMock { userService.findUserWithThrow(testAdminId) }
        verifyMock { userItemRepository.save(any()) }
        verifyMock {
            eventPublisher.sendActionEvent(capture(eventSlot), any(), eq("item-issue-events"))
        }

        val capturedEvent = eventSlot.captured
        assert(capturedEvent.itemId == testItemId)
        assert(capturedEvent.receiverId == testUserId)
        assert(capturedEvent.issuedBy == testAdminId)
    }

    @Test
    fun `issueForPlayerFromAdmin should throw exception when user does not exist`() {
        every { userService.findUserWithThrow(testUserId) } throws
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found",
            )

        assertThrows<ResponseStatusException> {
            userItemService.issueForPlayerFromAdmin(testUserId, testItemId, testAdminId)
        }

        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock(exactly = 0) { gameItemService.findById(any()) }
        verifyMock(exactly = 0) { userItemRepository.save(any()) }
        verifyMock(exactly = 0) { eventPublisher.sendActionEvent(any(), any(), any()) }
    }

    @Test
    fun `issueForPlayerFromAdmin should throw exception when item does not exist`() {
        every { gameItemService.findById(testItemId) } throws
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Item not found",
            )

        assertThrows<ResponseStatusException> {
            userItemService.issueForPlayerFromAdmin(testUserId, testItemId, testAdminId)
        }

        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock { gameItemService.findById(testItemId) }
        verifyMock { userService.findUserWithThrow(any<Long>()) }
        verifyMock(exactly = 0) { userItemRepository.save(any()) }
        verifyMock(exactly = 0) { eventPublisher.sendActionEvent(any(), any(), any()) }
    }

    @Test
    fun `issueForPlayerFromAdmin should throw exception when admin does not exist`() {
        every { userService.findUserWithThrow(testAdminId) } throws (
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found",
            )
        )

        val exception =
            assertThrows<ResponseStatusException> {
                userItemService.issueForPlayerFromAdmin(testUserId, testItemId, testAdminId)
            }

        assert(exception.statusCode == HttpStatus.NOT_FOUND)
        assert(exception.reason == "User not found")

        verifyMock { userService.findUserWithThrow(testUserId) }
        verifyMock { gameItemService.findById(testItemId) }
        verifyMock { userService.findUserWithThrow(testAdminId) }
        verifyMock(exactly = 0) { userItemRepository.save(any()) }
        verifyMock(exactly = 0) { eventPublisher.sendActionEvent(any(), any(), any()) }
    }
}

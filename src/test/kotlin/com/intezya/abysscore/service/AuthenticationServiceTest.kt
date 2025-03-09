package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.jwt.JwtUtils
import com.intezya.abysscore.security.password.PasswordUtils
import com.intezya.abysscore.security.service.AuthenticationService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.Test

class AuthenticationServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordUtils: PasswordUtils

    @MockK
    private lateinit var jwtUtils: JwtUtils

    @MockK
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var authenticationService: AuthenticationService

    private val testUsername = "testUser"
    private val testPassword = "password123"
    private val testHwid = "test-hwid-123"
    private val hashedPassword = "hashedPassword123"
    private val hashedHwid = "hashedHwid123"
    private val testIp = "192.168.1.1"
    private val testToken = "jwt-token-123"
    private val testUser =
        User(
            id = 1L,
            username = testUsername,
            password = hashedPassword,
            hwid = hashedHwid,
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { passwordUtils.hashPassword(any()) } returns hashedPassword
        every { passwordUtils.hashHwid(any()) } returns hashedHwid
        every { passwordUtils.verifyPassword(any(), any()) } returns true
        every { passwordUtils.verifyHwid(any(), any()) } returns true

        every { jwtUtils.generateJwtToken(any()) } returns testToken

        every { eventPublisher.sendActionEvent(any(), any(), any()) } just Runs
    }

    @Test
    fun `register user successfully`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)

        every { userRepository.save(any()) } returns testUser

        val response = authenticationService.registerUser(request, testIp)

        verify {
            userRepository.save(any())
            passwordUtils.hashPassword(testPassword)
            passwordUtils.hashHwid(testHwid)
            jwtUtils.generateJwtToken(testUser)
        }

        assertEquals(testToken, response.token)
    }

    @Test
    fun `register user with existing username`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)
        val exception = RuntimeException("uc_users_username violation")

        every { userRepository.save(any()) } throws exception

        val responseEx =
            assertThrows<ResponseStatusException> {
                authenticationService.registerUser(request, testIp)
            }

        assertEquals(HttpStatus.CONFLICT, responseEx.statusCode)
        assertEquals("User already exists", responseEx.reason)
    }

    @Test
    fun `register user with existing hwid`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)
        val exception = RuntimeException("uc_users_hwid violation")

        every { userRepository.save(any()) } throws exception

        val responseEx =
            assertThrows<ResponseStatusException> {
                authenticationService.registerUser(request, testIp)
            }

        assertEquals(HttpStatus.CONFLICT, responseEx.statusCode)
        assertEquals("Only 1 account allowed per device", responseEx.reason)
    }

    @Test
    fun `login user successfully`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)

        every { userRepository.findByUsername(testUsername) } returns Optional.of(testUser)

        val response = authenticationService.loginUser(request, testIp)

        verify {
            userRepository.findByUsername(testUsername)
            passwordUtils.verifyPassword(testPassword, hashedPassword)
            passwordUtils.verifyHwid(testHwid, hashedHwid)
            jwtUtils.generateJwtToken(testUser)
        }

        assertEquals(testToken, response.token)
    }

    @Test
    fun `login user not found`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)

        every { userRepository.findByUsername(testUsername) } returns Optional.empty()

        val responseEx =
            assertThrows<ResponseStatusException> {
                authenticationService.loginUser(request, testIp)
            }

        assertEquals(HttpStatus.NOT_FOUND, responseEx.statusCode)
        assertEquals("User not found", responseEx.reason)
    }

    @Test
    fun `login user with invalid password`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)

        every { userRepository.findByUsername(testUsername) } returns Optional.of(testUser)
        every { passwordUtils.verifyPassword(testPassword, hashedPassword) } returns false

        val responseEx =
            assertThrows<ResponseStatusException> {
                authenticationService.loginUser(request, testIp)
            }

        assertEquals(HttpStatus.UNAUTHORIZED, responseEx.statusCode)
        assertEquals("Invalid password", responseEx.reason)
    }

    @Test
    fun `login user with invalid hwid`() {
        val request = UserAuthRequest(username = testUsername, password = testPassword, hwid = testHwid)

        every { userRepository.findByUsername(testUsername) } returns Optional.of(testUser)
        every { passwordUtils.verifyHwid(testHwid, hashedHwid) } returns false

        val responseEx =
            assertThrows<ResponseStatusException> {
                authenticationService.loginUser(request, testIp)
            }

        assertEquals(HttpStatus.UNAUTHORIZED, responseEx.statusCode)
        assertEquals("Invalid hardware ID", responseEx.reason)
    }
}

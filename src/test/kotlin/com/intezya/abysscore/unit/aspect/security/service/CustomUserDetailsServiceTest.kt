package com.intezya.abysscore.unit.aspect.security.service

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.service.CustomUserDetailsService
import com.intezya.abysscore.security.utils.PasswordUtils
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.TestComponent
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

@TestComponent
@ExtendWith(MockKExtension::class)
class CustomUserDetailsServiceTest {
    @MockK
    private lateinit var passwordUtils: PasswordUtils

    private lateinit var userRepository: UserRepository
    private lateinit var userDetailsService: CustomUserDetailsService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userDetailsService = CustomUserDetailsService(userRepository, passwordUtils)
    }

    @Test
    fun `loadUserByUsername should return user when found`() {
        val username = "testUser"
        val mockUser = mockk<User>()

        every { userRepository.findByUsername(username) } returns Optional.of(mockUser)
        val result = userDetailsService.loadUserByUsername(username)

        assertEquals(mockUser, result)
        verify(exactly = 1) { userRepository.findByUsername(username) }
    }

    @Test
    fun `loadUserByUsername should throw UsernameNotFoundException when user not found`() {
        val username = "nonExistentUser"

        every { userRepository.findByUsername(username) } returns Optional.empty()

        val exception = assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername(username)
        }
        assertEquals("User not found", exception.message)
        verify(exactly = 1) { userRepository.findByUsername(username) }
    }

    @Test
    fun `updateHwid should call repository method with correct parameters`() {
        val userId = 123L
        val hwid = "test-hardware-id-456"
        val hashedHwid = "hashed-hardware-id-123"

        every { passwordUtils.hashHwid(hwid) } returns hashedHwid
        justRun { userRepository.updateHwid(userId, hashedHwid) }

        userDetailsService.updateHwid(userId, hwid)

        verify { userRepository.updateHwid(userId, hashedHwid) }

        val mockUser = mockk<User>()
        every { mockUser.hwid } returns hashedHwid
        every { userRepository.findByUsername("testUser") } returns Optional.of(mockUser)

        assertEquals(
            hashedHwid,
            userDetailsService.loadUserByUsername("testUser").hwid,
            "HWID should be updated",
        )
    }

    // TODO: Add test for HWID exception once implemented
    /*
    @Test
    fun `updateHwid should throw HwidException when appropriate`() {
        // This test will be implemented when the HWID exception handling is added
    }
     */
}

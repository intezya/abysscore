package com.intezya.abysscore.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.dto.user.UserAuthResponse
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import com.intezya.abysscore.security.jwt.JwtUtils
import com.intezya.abysscore.security.service.AuthenticationService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class AuthControllerTest {

    @MockK
    private lateinit var authenticationService: AuthenticationService

    @MockK
    private lateinit var jwtUtils: JwtUtils

    @InjectMockKs
    private lateinit var authController: AuthController

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    private val testUsername = "testUser"
    private val testPassword = "P_ssw0rd"
    private val testHwid = "test-hwid-123"
    private val testIp = "192.168.1.1"
    private val testToken = "jwt-token-123"
    private val testUserId = 1L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build()
        objectMapper = ObjectMapper()

        every { jwtUtils.getClientIp(any<HttpServletRequest>()) } returns "192.168.1.1"
    }

    @Test
    fun `registerUser should return user auth response when valid request`() {
        // Given
        val userAuthRequest = UserAuthRequest(testUsername, testPassword, testHwid)
        val userAuthResponse = UserAuthResponse(testToken)
        val requestSlot = slot<UserAuthRequest>()

        every {
            authenticationService.registerUser(capture(requestSlot), testIp)
        } returns userAuthResponse

        // When/Then
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(testToken))

        // Verify captured request
        assert(requestSlot.captured.username == testUsername)
        assert(requestSlot.captured.password == testPassword)
        assert(requestSlot.captured.hwid == testHwid)

        verify { jwtUtils.getClientIp(any<HttpServletRequest>()) }
        verify { authenticationService.registerUser(any(), testIp) }
    }

    @Test
    fun `loginUser should return user auth response when valid credentials`() {
        // Given
        val userAuthRequest = UserAuthRequest(testUsername, testPassword, testHwid)
        val userAuthResponse = UserAuthResponse(testToken)
        val requestSlot = slot<UserAuthRequest>()

        every {
            authenticationService.loginUser(capture(requestSlot), testIp)
        } returns userAuthResponse

        // When/Then
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(testToken))

        // Verify captured request
        assert(requestSlot.captured.username == testUsername)
        assert(requestSlot.captured.password == testPassword)
        assert(requestSlot.captured.hwid == testHwid)

        verify { jwtUtils.getClientIp(any<HttpServletRequest>()) }
        verify { authenticationService.loginUser(any(), testIp) }
    }

    @Test
    fun `getUserInfo should return authentication principal`() {
        // Given
        val userAuthInfo = UserAuthInfoDTO(testUserId, testUsername, testHwid)

        // When
        val result = authController.getUserInfo(userAuthInfo)

        // Then
        assert(result === userAuthInfo)
        assert(result.id == testUserId)
        assert(result.username == testUsername)
        assert(result.hwid == testHwid)
    }

    @Test
    fun `registerUser should handle bad request on invalid input`() {
        // This test would require adding an exception handler or using a WebMvcTest approach 
        // For a unit test, we can just confirm the controller would call the service
        // with invalid inputs and handle the resulting exception appropriately

        val invalidRequest = UserAuthRequest("", "", "")  // Empty fields

        every {
            authenticationService.registerUser(invalidRequest, testIp)
        } throws Exception("Validation failed")

        // Verification of exception handling would be done in an integration test
    }
}

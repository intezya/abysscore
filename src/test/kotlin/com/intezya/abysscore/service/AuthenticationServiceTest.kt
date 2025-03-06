package com.intezya.abysscore.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.repository.AdminRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.utils.auth.AuthUtils
import com.intezya.abysscore.utils.auth.PasswordUtils
import com.intezya.abysscore.utils.constructors.constructAuthRequest
import com.intezya.abysscore.utils.constructors.constructUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import kotlin.test.assertEquals


class AuthenticationServiceTest(
    private val passwordUtils: PasswordUtils,
    private val authUtils: AuthUtils,
    private val adminRepository: AdminRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {
    private val userRepository = mockk<UserRepository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val authenticationService = AuthenticationService(
        userRepository = userRepository,
        passwordUtils = passwordUtils,
        authUtils = authUtils,
        adminRepository = adminRepository,
        eventPublisher = eventPublisher,
    )

    @Test
    fun `create user with valid data`() {
        val testUser = constructUser()
        val registerRequest = constructAuthRequest()

        every { userRepository.save(any()) } returns testUser
        every { eventPublisher.sendActionEvent(any()) } does nothing

        val user = authenticationService.registerUser(registerRequest, ip = "test")

        val authData = this.authUtils.getUserInfoFromToken(user.token)

        assertEquals(testUser.id, authData.id)
        assertEquals(testUser.username, authData.username)
        assertEquals(testUser.hwid, authData.hwid)

        verify { userRepository.save(any()) }
    }
}

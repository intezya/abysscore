package com.intezya.abysscore.service

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.utils.auth.AuthUtils
import com.intezya.abysscore.utils.providers.RandomProvider
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertFailsWith


@SpringBootTest
@Import(TestPostgresConfiguration::class)
class AuthenticationServiceTest : BaseServiceTest() {
    private val userRepository: UserRepository = mockk<UserRepository>()
    private val eventPublisher: EventPublisher = mockk<EventPublisher>()

    @Autowired
    private lateinit var authUtils: AuthUtils

    @SpykBean
    private lateinit var authenticationService: AuthenticationService

    @Test
    fun `create user with valid data`() {
        val testUser = RandomProvider.constructUser()
        val registerRequest = RandomProvider.constructAuthRequest(user = testUser)

        every { userRepository.save(any()) } returns testUser
        every { eventPublisher.sendActionEvent(any(), any(), any()) } returns Unit

        val response = authenticationService.registerUser(registerRequest, ip = "test")

        val authData = authUtils.getUserInfoFromToken(response.token)

        assertEquals(1, authData.id)
        assertEquals(testUser.username, authData.username)
    }

    @Test
    fun `create user that already exists`() {
        val registerRequest = RandomProvider.constructAuthRequest()

        every { eventPublisher.sendActionEvent(any(), any(), any()) } returns Unit

        authenticationService.registerUser(registerRequest, ip = RandomProvider.ipv4())

        assertFailsWith<ResponseStatusException>("User already exists") {
            authenticationService.registerUser(registerRequest, ip = RandomProvider.ipv4())
        }
    }

    @Test
    fun `create user that already has account`() {
        var registerRequest = RandomProvider.constructAuthRequest()

        every { eventPublisher.sendActionEvent(any(), any(), any()) } returns Unit

        authenticationService.registerUser(registerRequest, ip = RandomProvider.ipv4())

        registerRequest = RandomProvider.constructAuthRequest(hwid = registerRequest.hwid)

        assertFailsWith<ResponseStatusException>("User already exists") {
            authenticationService.registerUser(registerRequest, ip = RandomProvider.ipv4())
        }
    }

}

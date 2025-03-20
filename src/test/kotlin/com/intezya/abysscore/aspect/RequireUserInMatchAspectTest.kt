package com.intezya.abysscore.aspect

import com.intezya.abysscore.controller.annotations.RequireUserInMatch
import com.intezya.abysscore.controller.aspect.RequireUserInMatchAspect
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class RequireUserInMatchAspectTest {

    private val aspect = RequireUserInMatchAspect()
    private val joinPoint = mockk<ProceedingJoinPoint>(relaxed = true)
    private val annotation = mockk<RequireUserInMatch>()

    @BeforeEach
    fun setup() {
        clearMocks(joinPoint)
    }

    @Test
    fun `should throw exception when user is not in match but expected`() {
        val user = User()
        every { joinPoint.args } returns arrayOf(user)
        every { annotation.expectedThat } returns true

        val exception = assertThrows<ResponseStatusException> {
            aspect.checkUserInMatch(joinPoint, annotation)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("User is not in a match", exception.reason)
        verify(exactly = 0) { joinPoint.proceed(any()) }
    }

    @Test
    fun `should throw exception when user is in match but expected to be out`() {
        val user = User().apply { currentMatch = Match() }
        every { joinPoint.args } returns arrayOf(user)
        every { annotation.expectedThat } returns false

        val exception = assertThrows<ResponseStatusException> {
            aspect.checkUserInMatch(joinPoint, annotation)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("User is in a match", exception.reason)
        verify(exactly = 0) { joinPoint.proceed(any()) }
    }

    @Test
    fun `should proceed when user is in match and expected`() {
        val user = User().apply { currentMatch = Match() }
        every { joinPoint.args } returns arrayOf(user)
        every { annotation.expectedThat } returns true
        every { joinPoint.proceed(any()) } returns "success"

        val result = aspect.checkUserInMatch(joinPoint, annotation)

        assertEquals("success", result)
        verify(exactly = 1) { joinPoint.proceed(any()) }
    }

    @Test
    fun `should proceed when user is not in match and expected`() {
        val user = User()
        every { joinPoint.args } returns arrayOf(user)
        every { annotation.expectedThat } returns false
        every { joinPoint.proceed(any()) } returns "success"

        val result = aspect.checkUserInMatch(joinPoint, annotation)

        assertEquals("success", result)
        verify(exactly = 1) { joinPoint.proceed(any()) }
    }

    @Test
    fun `should throw exception when no user is found in arguments`() {
        every { joinPoint.args } returns arrayOf("invalid argument")

        val exception = assertThrows<IllegalArgumentException> {
            aspect.checkUserInMatch(joinPoint, annotation)
        }

        assertEquals("User not found in arguments", exception.message)
        verify(exactly = 0) { joinPoint.proceed(any()) }
    }
}

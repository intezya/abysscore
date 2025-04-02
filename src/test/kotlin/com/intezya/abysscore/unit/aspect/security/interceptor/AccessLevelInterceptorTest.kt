package com.intezya.abysscore.unit.aspect.security.interceptor

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.security.interceptor.AccessLevelInterceptor
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.method.HandlerMethod
import org.springframework.web.server.ResponseStatusException
import kotlin.reflect.jvm.javaMethod

class AccessLevelInterceptorTest {
    private lateinit var interceptor: AccessLevelInterceptor
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var handlerMethod: HandlerMethod
    private lateinit var securityContext: SecurityContext
    private lateinit var authentication: Authentication

    @BeforeEach
    fun setup() {
        interceptor = AccessLevelInterceptor()
        request = mockk()
        response = mockk()

        val method = TestController::annotatedMethod.javaMethod!!
        handlerMethod = mockk<HandlerMethod>()
        every { handlerMethod.method } returns method

        // Mock for SecurityContext and Authentication
        securityContext = mockk<SecurityContext>()
        authentication = mockk<Authentication>()

        // Set up the security context holder
        SecurityContextHolder.setContext(securityContext)
    }

    @Test
    fun `should allow access when no annotation is present`() {
        // Mock method without annotation
        val methodWithoutAnnotation = TestController::methodWithoutAnnotation.javaMethod!!
        val handlerWithoutAnnotation = mockk<HandlerMethod>()
        every { handlerWithoutAnnotation.method } returns methodWithoutAnnotation

        // Execute
        val result = interceptor.preHandle(request, response, handlerWithoutAnnotation)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `should allow access when user has sufficient access level`() {
        val user = User().apply { accessLevel = AccessLevel.ADMIN }

        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns user

        val result = interceptor.preHandle(request, response, handlerMethod)

        assertTrue(result)
    }

    @Test
    fun `should throw exception when user has insufficient access level`() {
        val user = User().apply { accessLevel = AccessLevel.USER }

        every { securityContext.authentication } returns authentication
        every { authentication.principal } returns user

        val exception = assertThrows<ResponseStatusException> {
            interceptor.preHandle(request, response, handlerMethod)
        }

        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        assertTrue(exception.reason?.contains("Access denied") ?: false)
        assertTrue(exception.reason?.contains("Required level: ${AccessLevel.ADMIN.value}") ?: false)
        assertTrue(exception.reason?.contains("your level: ${AccessLevel.USER.value}") ?: false)
    }

    @Test
    fun `should handle non-HandlerMethod handlers`() {
        val nonHandlerMethod = Object()

        val result = interceptor.preHandle(request, response, nonHandlerMethod)

        assertTrue(result)
    }

    class TestController {
        @RequiresAccessLevel(level = AccessLevel.ADMIN)
        fun annotatedMethod() {
        }

        fun methodWithoutAnnotation() {}
    }
}

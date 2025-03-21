package com.intezya.abysscore.security.annotation

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test

class RequiresAccessLevelTest {
    @Test
    fun `annotation properties should be accessible`() {
        val method = TestController::annotatedMethod.javaMethod

        val annotation = method?.getAnnotation(RequiresAccessLevel::class.java)

        assertNotNull(annotation)
        assertEquals(AccessLevel.ADMIN, annotation?.level)
    }

    class TestController {
        @RequiresAccessLevel(level = AccessLevel.ADMIN)
        fun annotatedMethod() {
        }
    }
}

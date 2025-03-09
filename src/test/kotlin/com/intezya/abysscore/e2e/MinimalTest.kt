package com.intezya.abysscore.e2e

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPostgresConfiguration::class)
class MinimalTest {
    @Test
    fun `context loads`() {
        // Empty test to verify context loading
    }
}

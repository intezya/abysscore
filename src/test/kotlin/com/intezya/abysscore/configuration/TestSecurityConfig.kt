package com.intezya.abysscore.configuration

import com.intezya.abysscore.security.jwt.JwtUtils
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.*

@TestConfiguration
class TestSecurityConfig {
    @Bean
    fun jwtUtils(): JwtUtils =
        JwtUtils(
            jwtSecret = UUID.randomUUID().toString(),
            jwtExpirationMs = 10000000,
        )
}

package com.intezya.abysscore.config

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArgonConfig {
    companion object {
        private const val SALT_LENGTH = 16
        private const val HASH_LENGTH = 32
    }

    @Bean
    fun argon2(): Argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id, // Type of Argon2 variant
        SALT_LENGTH, // Salt length
        HASH_LENGTH, // Hash length
    )
}

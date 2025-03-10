package com.intezya.abysscore.config

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val SALT_LENGTH = 16
private const val HASH_LENGTH = 32

@Configuration
class ArgonConfig {
    @Bean
    fun argon2(): Argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id,
        SALT_LENGTH,
        HASH_LENGTH,
    )
}

package com.intezya.abysscore.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration
class TestPostgresConfiguration {
    companion object {
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:17")
                .withUsername("test")
                .withPassword("test")
                .withDatabaseName("testdb")

        init {
            postgres.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        }
    }
}

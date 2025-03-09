package com.intezya.abysscore.configuration

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestConfiguration
class TestPostgresConfiguration {
    @Bean
    fun dataSource(): DataSource {
        val container =
            PostgreSQLContainer("postgres:17")
                .withUsername("test")
                .withPassword("test")
                .withDatabaseName("testdb")

        container.start()

        return HikariDataSource().apply {
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
        }
    }
}

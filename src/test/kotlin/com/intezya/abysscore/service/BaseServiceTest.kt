package com.intezya.abysscore.service

import com.intezya.abysscore.configuration.TestPostgresConfiguration
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(TestPostgresConfiguration::class)
abstract class BaseServiceTest {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun clearDatabase() {
        val tables =
            jdbcTemplate.queryForList("SELECT tablename FROM pg_tables WHERE schemaname = 'public'", String::class.java)

        tables.forEach {
            jdbcTemplate.execute("TRUNCATE TABLE $it RESTART IDENTITY CASCADE;")
        }
    }

}

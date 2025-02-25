package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun snakeCaseObjectMapper(): ObjectMapper {
        return Json.mapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
    }

    @Bean
    fun modelResolver(snakeCaseObjectMapper: ObjectMapper): ModelResolver {
        return ModelResolver(snakeCaseObjectMapper)
    }
}

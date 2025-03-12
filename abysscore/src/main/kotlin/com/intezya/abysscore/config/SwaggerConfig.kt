package com.intezya.abysscore.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun snakeCaseObjectMapper(): ObjectMapper = Json.mapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    @Bean
    fun modelResolver(snakeCaseObjectMapper: ObjectMapper): ModelResolver = ModelResolver(snakeCaseObjectMapper)

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .components(
            Components()
                .addSecuritySchemes(
                    "bearer-jwt",
                    io.swagger.v3.oas.models.security
                        .SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .`in`(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name("Authorization"),
                ),
        ).info(
            Info()
                .title("API Documentation")
                .description("API для приложения")
                .version("1.0"),
        ).addSecurityItem(
            SecurityRequirement()
                .addList("bearer-jwt", emptyList()),
        )
}

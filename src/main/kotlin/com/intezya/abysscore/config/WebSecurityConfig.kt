package com.intezya.abysscore.config

import com.intezya.abysscore.middleware.AuthTokenFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    companion object {
        private val PUBLIC_PATHS =
            arrayOf(
//            "/**", // dev
                "/auth/register",
                "/auth/login",
                "/auth/admin/login",
                "/swagger-ui/**",
                "/api-docs/**",
                "/swagger-ui.html",
            )
    }

    @Bean
    fun authenticationJwtTokenFilter(): AuthTokenFilter = AuthTokenFilter()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.disable() } // Disable CORS if not needed
            .csrf { it.disable() } // Disable CSRF for REST APIs
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*PUBLIC_PATHS)
                    .permitAll() // Public paths configuration
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll() // Allow OPTIONS requests
                    .anyRequest()
                    .authenticated()
            }.addFilterBefore(
                authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter::class.java,
            ).exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf("*")
                allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
            }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

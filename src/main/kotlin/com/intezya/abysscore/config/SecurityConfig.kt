package com.intezya.abysscore.config

import com.intezya.abysscore.security.middleware.JwtAuthenticationFilter
import com.intezya.abysscore.security.public.PUBLIC_PATHS
import com.intezya.abysscore.security.service.CustomAuthenticationProvider
import com.intezya.abysscore.security.service.CustomUserDetailsService
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.disable() } // Disable CORS if not needed
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(*PUBLIC_PATHS)
                    .permitAll() // Public paths configuration
                    .requestMatchers("/swagger-ui/**") // Отдельно указываем путь с wildcard
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll() // Allow OPTIONS requests
                    .anyRequest()
                    .authenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationProvider(
        userDetailsService: CustomUserDetailsService,
        passwordUtils: PasswordUtils,
    ): AuthenticationProvider = CustomAuthenticationProvider(userDetailsService, passwordUtils)

    @Bean
    fun authenticationManager(
        http: HttpSecurity,
        authenticationProvider: AuthenticationProvider,
    ): AuthenticationManager {
        val authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)

        authManagerBuilder.authenticationProvider(authenticationProvider)

        return authManagerBuilder.build()
    }
}

package com.intezya.abysscore.security.service

import com.intezya.abysscore.security.utils.JwtUtils
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.LogFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service

private const val BEARER_PREFIX = "Bearer "


@Service
class JwtAuthenticationService(
    private val jwtService: JwtUtils,
    private val userDetailsService: CustomUserDetailsService,
) {
    private val log = LogFactory.getLog(this.javaClass)

    fun authenticateWithToken(jwt: String): Pair<Boolean, UserDetails?> {
        try {
            val username = jwtService.extractUsername(jwt)

            val userDetails = try {
                userDetailsService.loadUserByUsername(username)
            } catch (e: UsernameNotFoundException) {
                log.warn("User not found: $username")
                return Pair(false, null)
            }

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("Invalid JWT token for user: $username")
                return Pair(false, null)
            }

            log.debug("User authenticated successfully: $username")
            return Pair(true, userDetails)
        } catch (e: ExpiredJwtException) {
            log.warn("JWT token expired")
            return Pair(false, null)
        } catch (e: MalformedJwtException) {
            log.warn("Invalid JWT token format")
            return Pair(false, null)
        } catch (e: JwtException) {
            log.warn("JWT exception: ${e.message}")
            return Pair(false, null)
        } catch (e: Exception) {
            log.error("Unexpected error during authentication", e)
            return Pair(false, null)
        }
    }

    fun createAuthenticationToken(userDetails: UserDetails, request: HttpServletRequest? = null): Authentication {
        val authToken = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
        )

        if (request != null) {
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        }

        return authToken
    }

    fun extractJwtFromHeader(authHeader: String?): Pair<Boolean, String> {
        if (authHeader == null) {
            log.debug("No Authentication header found or invalid format")
            return Pair(false, "Authentication required")
        }

        val jwt = authHeader.replace(BEARER_PREFIX, "")

        if (jwt.isBlank()) {
            log.warn("Empty JWT token provided")
            return Pair(false, "Empty token")
        }

        if (jwt.length > 10000) {
            log.warn("JWT token exceeds maximum allowed length")
            return Pair(false, "Invalid token format")
        }

        return Pair(true, jwt)
    }
}

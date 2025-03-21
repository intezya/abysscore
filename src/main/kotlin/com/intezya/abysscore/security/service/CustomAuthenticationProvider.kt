package com.intezya.abysscore.security.service

import com.intezya.abysscore.security.utils.CustomAuthenticationToken
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
    private val userDetailsService: CustomUserDetailsService,
    private val passwordUtils: PasswordUtils,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val password = authentication.credentials as String

        val userDetails = userDetailsService.loadUserByUsername(username)

        if (!passwordUtils.verifyPassword(password, userDetails.password)) {
            throw BadCredentialsException("Wrong password")
        }

        if (authentication !is CustomAuthenticationToken) {
            throw BadCredentialsException("Invalid authentication type")
        }

        val hwidAsAdditionalField = authentication.hwidAdditionalField

        if (userDetails.hwid != null && !passwordUtils.verifyHwid(hwidAsAdditionalField, userDetails.hwid!!)) {
            throw BadCredentialsException("Invalid hardware ID")
        }
        if (userDetails.hwid == null) {
            userDetails.hwid = passwordUtils.hashHwid(hwidAsAdditionalField)
            userDetailsService.updateHwid(userDetails.id, hwidAsAdditionalField)
        }

        if (!userDetails.isAccountNonExpired) {
            throw BadCredentialsException("User is expired")
        }

        if (!userDetails.isAccountNonLocked) {
            throw BadCredentialsException("User is locked")
        }

        if (!userDetails.isEnabled) {
            throw BadCredentialsException("User is disabled")
        }

        return CustomAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities,
            hwidAsAdditionalField,
        )
    }

    override fun supports(authentication: Class<*>): Boolean = authentication == UsernamePasswordAuthenticationToken::class.java ||
        authentication == CustomAuthenticationToken::class.java
}

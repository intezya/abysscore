package com.intezya.abysscore.security.utils

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CustomAuthenticationToken(
    principal: Any,
    credentials: Any?,
    authorities: Collection<GrantedAuthority>? = null,
    val hwidAdditionalField: String,
) : UsernamePasswordAuthenticationToken(principal, credentials, authorities) {
    constructor(
        principal: Any,
        credentials: Any?,
        hwidAdditionalField: String,
    ) :
        this(principal, credentials, null, hwidAdditionalField)
}

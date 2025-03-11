package com.intezya.abysscore.security.service

import com.intezya.abysscore.model.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthDTO(
    val id: Long,
    private val username: String,
    private val password: String,
    var hwid: String?,
    val accessLevel: Int,

) : UserDetails {
    constructor(user: User) : this(
        id = user.id!!,
        username = user.username,
        password = user.password,
        hwid = user.hwid,
        accessLevel = user.accessLevel.value,
    )

    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}

fun User.toAuthDTO(): AuthDTO = AuthDTO(this)

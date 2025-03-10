package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.utils.converter.AccessLevelConverter
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    private val username: String,
    @Column(nullable = false)
    private val password: String,
    @Column(unique = true)
    var hwid: String?,
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false, updatable = false)
    @Convert(converter = AccessLevelConverter::class)
    var accessLevel: AccessLevel = AccessLevel.USER,
) : UserDetails {
    constructor() : this(null, "", "", "", LocalDateTime.now(), LocalDateTime.now(), AccessLevel.USER)

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${accessLevel.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}

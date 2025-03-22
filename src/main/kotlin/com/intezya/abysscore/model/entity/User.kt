package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.utils.converter.AccessLevelConverter
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(unique = true)
    private val username: String

    @Column(nullable = false)
    private val password: String

    @Column(unique = true)
    var hwid: String?

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false, updatable = false)
    @Convert(converter = AccessLevelConverter::class)
    var accessLevel: AccessLevel = AccessLevel.USER

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableSet<UserItem> = mutableSetOf()

    var receiveMatchInvites: Boolean = false

    @OneToMany(mappedBy = "inviter", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val sentInvites: MutableSet<MatchInvite> = mutableSetOf()

    @OneToMany(mappedBy = "invitee", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val receivedInvites: MutableSet<MatchInvite> = mutableSetOf()

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_match_id", nullable = true)
    var currentMatch: Match? = null

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_badge_id", nullable = true)
    var currentBadge: UserItem? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var globalStatistic: UserGlobalStatistic

    constructor() : this(
        username = "",
        password = "",
        hwid = null,
    )

    constructor(
        username: String,
        password: String,
        hwid: String?,
        accessLevel: AccessLevel = AccessLevel.USER,
        receiveMatchInvites: Boolean = false,
    ) {
        this.username = username
        this.password = password
        this.hwid = hwid
        this.accessLevel = accessLevel
        this.receiveMatchInvites = receiveMatchInvites
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            username == other.username && hwid == other.hwid
        }
    }

    override fun hashCode(): Int = Objects.hash(id, username)

    override fun toString(): String = this::class.simpleName +
        "(id = $id , username = $username , password = $password , hwid = $hwid , createdAt = $createdAt , updatedAt = $updatedAt , accessLevel = $accessLevel , receiveMatchInvites = $receiveMatchInvites )"

    fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_USER"))

    fun getPassword(): String = password

    fun getUsername(): String = username

    fun isAccountNonExpired(): Boolean = true

    fun isAccountNonLocked(): Boolean = true

    fun isCredentialsNonExpired(): Boolean = true

    fun isEnabled(): Boolean = true
}

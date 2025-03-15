package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.utils.converter.AccessLevelConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(unique = true)
    var hwid: String?,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, updatable = false)
    @Convert(converter = AccessLevelConverter::class)
    var accessLevel: AccessLevel = AccessLevel.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableSet<UserItem> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "user_id")
    val globalStatistic: UserGlobalStatistic? = null,

    var receiveMatchInvites: Boolean = false,
) {
    constructor() : this(
        id = 0L,
        username = "",
        password = "",
        hwid = "",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        accessLevel = AccessLevel.USER,
        items = mutableSetOf(),
        globalStatistic = null,
        receiveMatchInvites = false,
    )

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

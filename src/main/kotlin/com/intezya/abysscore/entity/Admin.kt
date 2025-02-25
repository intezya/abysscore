package com.intezya.abysscore.entity

import com.intezya.abysscore.enum.AccessLevel
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "admins")
data class Admin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false, unique = true)
    val user: User,

    @Column(nullable = false, updatable = false, unique = true)
    var telegramId: Long,

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var accessLevel: AccessLevel = AccessLevel.ADMIN,

    @Column(nullable = false, updatable = false)
    val adminFrom: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, User(), 0, AccessLevel.ADMIN, LocalDateTime.now())
}

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

    @Column(nullable = false, updatable = false)
    var telegramId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var accessLevel: AccessLevel = AccessLevel.ADMIN,

    @Column(nullable = false, updatable = false)
    val adminFrom: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, 0, AccessLevel.ADMIN, LocalDateTime.now())
}

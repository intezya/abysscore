package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.utils.converters.AccessLevelConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "admins")
data class Admin(
    @Id
    var id: Long? = null,
    @OneToOne(cascade = [CascadeType.MERGE])
    @MapsId
    @JoinColumn(name = "id")
    val user: User,
    @Column(nullable = false, updatable = false, unique = true)
    var telegramId: Long,
    @Column(nullable = false, updatable = false)
    @Convert(converter = AccessLevelConverter::class)
    var accessLevel: AccessLevel = AccessLevel.ADMIN,
    @Column(nullable = false, updatable = false)
    val adminFrom: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, User(), 0, AccessLevel.ADMIN, LocalDateTime.now())
}

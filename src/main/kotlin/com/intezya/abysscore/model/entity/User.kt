package com.intezya.abysscore.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(unique = true)
    val hwid: String?,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, "", "", "", LocalDateTime.now(), LocalDateTime.now())

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

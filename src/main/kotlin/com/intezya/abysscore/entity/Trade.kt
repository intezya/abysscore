package com.intezya.abysscore.entity

import com.intezya.abysscore.enum.TradeStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "trades")
data class Trade(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    val initiator: User,

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TradeStatus = TradeStatus.PENDING,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(null, User(), User(), TradeStatus.PENDING, LocalDateTime.now(), LocalDateTime.now())
}

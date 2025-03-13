package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.ItemSourceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_items")
data class UserItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    val gameItem: GameItem? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    val receivedFrom: Trade? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    val sourceType: ItemSourceType = ItemSourceType.SYSTEM,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(
        0L,
        user = null,
        gameItem = null,
        receivedFrom = null,
        sourceType = ItemSourceType.SYSTEM,
        createdAt = LocalDateTime.now(),
    )
}

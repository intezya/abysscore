package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.ItemSourceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_items")
data class UserItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    val gameItem: GameItem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    val receivedFrom: Trade? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    val sourceType: ItemSourceType = ItemSourceType.SYSTEM,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, User(), GameItem(), null, ItemSourceType.SYSTEM, LocalDateTime.now())
}

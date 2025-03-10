package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.ItemSourceType
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_items")
data class UserItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    val gameItem: GameItem,
    @ManyToOne
    @JoinColumn(name = "trade_id")
    val receivedFrom: Trade? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    val sourceType: ItemSourceType = ItemSourceType.SYSTEM,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    constructor() : this(null, User(), GameItem(), null, ItemSourceType.SYSTEM, LocalDateTime.now())

    fun toDTO(): UserItemDTO = UserItemDTO(
        id = id!!,
        gameItem = gameItem,
        receivedFrom = receivedFrom?.id,
        sourceType = sourceType,
        createdAt = createdAt,
    )
}

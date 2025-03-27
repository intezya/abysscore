package com.intezya.abysscore.model.entity

import com.intezya.abysscore.enum.ItemSourceType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_items")
class UserItem(
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    val sourceType: ItemSourceType = ItemSourceType.SYSTEM,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    val receivedFrom: Trade? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    lateinit var gameItem: GameItem

    constructor() : this(ItemSourceType.SYSTEM)

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(sourceType, createdAt, user, gameItem, receivedFrom)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserItem) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            sourceType == other.sourceType &&
                createdAt == other.createdAt &&
                user == other.user &&
                gameItem == other.gameItem &&
                receivedFrom == other.receivedFrom
        }
    }

    override fun toString(): String =
        this::class.simpleName + "(id = $id , sourceType = $sourceType , createdAt = $createdAt )"
}

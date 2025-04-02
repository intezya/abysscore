package com.intezya.abysscore.model.entity.trade

import com.intezya.abysscore.enum.TradeStatus
import com.intezya.abysscore.model.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "trades")
class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TradeStatus = TradeStatus.PENDING

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    lateinit var initiator: User

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    lateinit var receiver: User

    constructor()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(status, createdAt, updatedAt, initiator, receiver)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Trade) return false

        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            status == other.status &&
                createdAt == other.createdAt &&
                updatedAt == other.updatedAt &&
                initiator == other.initiator &&
                receiver == other.receiver
        }
    }

    override fun toString(): String = this::class.simpleName +
        "(id = $id , status = $status , createdAt = $createdAt , updatedAt = $updatedAt , initiator = $initiator , receiver = $receiver )"
}

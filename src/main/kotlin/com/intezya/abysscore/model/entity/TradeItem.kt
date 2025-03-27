import com.intezya.abysscore.model.entity.Trade
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "trade_items")
class TradeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    @ManyToOne
    @JoinColumn(name = "trade_id", nullable = false)
    lateinit var trade: Trade

    @ManyToOne
    @JoinColumn(name = "user_item_id", nullable = false)
    lateinit var userItem: UserItem

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    lateinit var owner: User

    constructor()

    constructor(id: Long) {
        this.id = id
    }

    constructor(trade: Trade, userItem: UserItem, owner: User) {
        this.trade = trade
        this.userItem = userItem
        this.owner = owner
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TradeItem) return false
        return if (id != 0L && other.id != 0L) {
            id == other.id
        } else {
            trade == other.trade &&
                userItem == other.userItem &&
                owner == other.owner
        }
    }

    override fun hashCode(): Int = if (id != 0L) {
        id.hashCode()
    } else {
        Objects.hash(trade, userItem, owner)
    }

    override fun toString(): String =
        this::class.simpleName + "(id = $id , trade = $trade , userItem = $userItem , owner = $owner )"
}

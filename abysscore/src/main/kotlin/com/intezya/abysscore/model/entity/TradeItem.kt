import com.intezya.abysscore.model.entity.Trade
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import jakarta.persistence.*

@Entity
@Table(name = "trade_items")
data class TradeItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "trade_id", nullable = false)
    val trade: Trade,
    @ManyToOne
    @JoinColumn(name = "user_item_id", nullable = false)
    val userItem: UserItem,
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,
) {
    constructor() : this(null, Trade(), UserItem(), User())
}

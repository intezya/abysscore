import com.intezya.abysscore.model.entity.Trade
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserItem
import jakarta.persistence.*

@Entity
@Table(name = "trade_items")
data class TradeItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @ManyToOne
    @JoinColumn(name = "trade_id", nullable = false)
    val trade: Trade? = null,
    @ManyToOne
    @JoinColumn(name = "user_item_id", nullable = false)
    val userItem: UserItem? = null,
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User? = null,
) {
    constructor() : this(
        id = 0L,
        trade = null,
        userItem = null,
        owner = null,
    )
}

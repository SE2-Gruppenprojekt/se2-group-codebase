package at.se2group.backend.persistence

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "turn_draft_board_sets")
class TurnDraftBoardSetEntity(

    @Id
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id")
    var draft: TurnDraftEntity? = null,

    @ElementCollection
    @CollectionTable(
        name = "turn_draft_board_set_tiles",
        joinColumns = [JoinColumn(name = "board_set_id")]
    )
    @OrderColumn(name = "tile_order")
    var tiles: MutableList<TileEmbeddable> = mutableListOf()
)

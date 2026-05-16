package at.se2group.backend.persistence

import jakarta.persistence.*
import shared.models.game.domain.BoardSetType
import java.util.UUID

@Entity
@Table(name = "turn_draft_board_sets")
class TurnDraftBoardSetEntity(

    @Id
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id")
    var draft: TurnDraftEntity? = null,

    @Column(name = "board_set_id", nullable = false)
    var boardSetId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: BoardSetType = BoardSetType.UNRESOLVED,

    @ElementCollection
    @CollectionTable(
        name = "turn_draft_board_set_tiles",
        joinColumns = [JoinColumn(name = "board_set_id")]
    )
    @OrderColumn(name = "tile_order")
    var tiles: MutableList<TileEmbeddable> = mutableListOf()
)

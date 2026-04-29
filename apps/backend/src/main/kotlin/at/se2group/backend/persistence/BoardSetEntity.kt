package at.se2group.backend.persistence

import at.se2group.backend.domain.BoardSetType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table

@Entity
@Table(name = "game_board_sets")
class BoardSetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    var game: GameEntity? = null,

    @Column(name = "board_set_id", nullable = false)
    var boardSetId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: BoardSetType = BoardSetType.UNRESOLVED,

    @ElementCollection
    @CollectionTable(name = "game_board_set_tiles", joinColumns = [JoinColumn(name = "board_set_entity_id")])
    @OrderColumn(name = "tile_order")
    var tiles: MutableList<TileEmbeddable> = mutableListOf()
)

package at.se2group.backend.persistence

import jakarta.persistence.*

@Entity
class TurnDraftEntity(

    @Id
    var gameId: String = "",

    var playerUserId: String = "",

    var version: Long = 0,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "tileId",
            column = Column(name = "drawn_tile_id", nullable = true)
        ),
        AttributeOverride(
            name = "color",
            column = Column(name = "drawn_tile_color", nullable = true)
        ),
        AttributeOverride(
            name = "number",
            column = Column(name = "drawn_tile_number", nullable = true)
        ),
        AttributeOverride(
            name = "joker",
            column = Column(name = "drawn_tile_joker", nullable = true)
        )
    )
    var drawnTile: TileEmbeddable? = null,

    @ElementCollection
    var rackTiles: MutableList<TileEmbeddable> = mutableListOf(),

    @OneToMany(mappedBy = "draft", cascade = [CascadeType.ALL], orphanRemoval = true)
    var boardSets: MutableList<TurnDraftBoardSetEntity> = mutableListOf()
)

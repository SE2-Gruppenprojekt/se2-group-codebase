package at.se2group.backend.persistence

import jakarta.persistence.*

@Entity
class TurnDraftEntity(

    @Id
    var gameId: String = "",

    var playerUserId: String = "",

    @ElementCollection
    var rackTiles: MutableList<TileEmbeddable> = mutableListOf(),

    @ElementCollection
    var boardTiles: MutableList<TileEmbeddable> = mutableListOf()
)

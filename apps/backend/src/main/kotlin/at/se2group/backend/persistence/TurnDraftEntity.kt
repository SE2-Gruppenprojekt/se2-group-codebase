package at.se2group.backend.persistence

import jakarta.persistence.*

@Entity
class TurnDraftEntity(

    @Id
    var gameId: String = "",

    var playerUserId: String = "",

    var version: Long = 0,

    @ElementCollection
    var rackTiles: MutableList<TileEmbeddable> = mutableListOf(),

    @OneToMany(mappedBy = "draft", cascade = [CascadeType.ALL], orphanRemoval = true)
    var boardSets: MutableList<TurnDraftBoardSetEntity> = mutableListOf()
)

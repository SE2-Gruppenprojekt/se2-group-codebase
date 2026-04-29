package at.se2group.backend.persistence

import jakarta.persistence.*

@Entity
class TurnDraftEntity(

    @Id
    var gameId: String = "",

    var playerUserId: String = "",

    @ElementCollection
    var boardSets: MutableList<String> = mutableListOf(),

    @ElementCollection
    var rackTiles: MutableList<String> = mutableListOf()
)

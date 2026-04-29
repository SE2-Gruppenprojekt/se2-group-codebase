package at.se2group.backend.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class TurnDraftEntity(

    @Id
    var gameId: String,

    var playerUserId: String
)

package at.se2group.backend.domain

data class GameStartResult(
    val game: Game,
    val turnDraft: TurnDraft? = null
)


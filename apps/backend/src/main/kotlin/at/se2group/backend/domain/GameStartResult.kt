package at.se2group.backend.domain

data class GameStartResult(
    val confirmedGame: ConfirmedGame,
    val turnDraft: TurnDraft? = null
)

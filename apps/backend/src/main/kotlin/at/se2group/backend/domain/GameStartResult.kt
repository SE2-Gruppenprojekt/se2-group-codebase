package at.se2group.backend.domain

import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.TurnDraft

data class GameStartResult(
    val confirmedGame: ConfirmedGame,
    val turnDraft: TurnDraft? = null
)

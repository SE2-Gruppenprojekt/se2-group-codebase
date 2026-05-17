package shared.models.game.event

import shared.models.EventPayload

data class TurnChangedEvent(
    val gameId: String,
    val currentTurnPlayerId: String
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "turn.changed"
    }
}

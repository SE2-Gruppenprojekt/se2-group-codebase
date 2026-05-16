package shared.models.game.event

import shared.models.EventPayload

data class TurnTimedOutEvent(
    val gameId: String,
    val previousTurnPlayerId: String
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "turn.timed_out"
    }
}

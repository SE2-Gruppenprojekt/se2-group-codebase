package shared.models.game.event

import shared.models.EventPayload

data class GameEndedEvent(
    val gameId: String,
    val winnerUserId: String
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "game.ended"
    }
}

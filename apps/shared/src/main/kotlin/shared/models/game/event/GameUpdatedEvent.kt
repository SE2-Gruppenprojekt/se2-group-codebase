package shared.models.game.event

import shared.models.EventPayload
import shared.models.game.response.GameResponse

data class GameUpdatedEvent(
    val gameId: String,
    val game: GameResponse
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "game.updated"
    }
}

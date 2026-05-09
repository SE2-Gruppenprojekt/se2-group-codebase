package shared.models.game.event

import shared.models.game.response.GameResponse

data class GameUpdatedEvent(
    val gameId: String,
    val game: GameResponse
) : GameEvent {
    override val type = TYPE

    companion object {
        const val TYPE = "game.updated"
    }
}

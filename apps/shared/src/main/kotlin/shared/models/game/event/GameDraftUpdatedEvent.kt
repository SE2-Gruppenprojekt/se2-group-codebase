package shared.models.game.event

import shared.models.game.response.TurnDraftResponse

data class GameDraftUpdatedEvent(
    val gameId: String,
    val playerId: String,
    val draft: TurnDraftResponse
) : GameEvent {
    override val type = TYPE

    companion object {
        const val TYPE = "game.draft.updated"
    }
}

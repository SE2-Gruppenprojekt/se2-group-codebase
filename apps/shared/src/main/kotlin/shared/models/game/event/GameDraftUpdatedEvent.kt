package shared.models.game.event

import shared.models.EventPayload
import shared.models.game.response.TurnDraftResponse

data class GameDraftUpdatedEvent(
    val gameId: String,
    val playerId: String,
    val draft: TurnDraftResponse
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "game.draft.updated"
    }
}

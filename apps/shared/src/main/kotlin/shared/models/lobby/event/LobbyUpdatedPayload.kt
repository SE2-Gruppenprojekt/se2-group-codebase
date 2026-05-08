package shared.models.lobby.event

import shared.models.EventPayload
import shared.models.lobby.response.LobbyResponse

data class LobbyUpdatedPayload(
    val lobby: LobbyResponse
)  : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "lobby.updated"
    }
}


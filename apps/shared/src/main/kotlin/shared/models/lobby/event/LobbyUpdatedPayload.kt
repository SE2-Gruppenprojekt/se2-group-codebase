package shared.models.lobby.event

import shared.models.EventPayLoad
import shared.models.lobby.response.LobbyResponse

data class LobbyUpdatedPayload(
    val lobby: LobbyResponse
) : EventPayLoad(TYPE) {
    companion object {
        const val TYPE = "lobby.updated"
    }
}

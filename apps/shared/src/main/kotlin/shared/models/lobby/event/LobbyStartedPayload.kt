package shared.models.lobby.event

import shared.models.EventPayLoad

data class LobbyStartedPayload(
    val lobbyId: String,
    val matchId: String
) : EventPayLoad(TYPE) {
    companion object {
        const val TYPE = "lobby.started"
    }
}

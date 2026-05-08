package shared.models.lobby.event

import shared.models.EventPayload

data class LobbyStartedPayload(
    val lobbyId: String,
    val matchId: String
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "lobby.started"
    }
}

package shared.models.lobby.event

import shared.models.EventPayLoad

data class LobbyDeletedPayload(
    val lobbyId: String
) : EventPayLoad(TYPE) {
    companion object {
        const val TYPE = "lobby.deleted"
    }
}

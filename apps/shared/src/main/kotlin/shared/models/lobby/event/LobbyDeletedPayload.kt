package shared.models.lobby.event

import shared.models.EventPayload

data class LobbyDeletedPayload(
    val lobbyId: String
) : EventPayload {
    override val type = TYPE

    companion object {
        const val TYPE = "lobby.deleted"
    }
}

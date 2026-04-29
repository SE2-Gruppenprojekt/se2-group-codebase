package at.aau.serg.android.core.network.lobby

import shared.models.lobby.response.LobbyResponse

open class EventPayLoad(
    val type: String
)

data class LobbyUpdatedPayload(
    val lobby: LobbyResponse
) : EventPayLoad(type = "lobby.updated")

data class LobbyDeletedPayload(
    val lobbyId: String
) : EventPayLoad(type = "lobby.deleted")

data class LobbyStartedPayload(
    val lobbyId: String,
    val matchId: String
) : EventPayLoad(type = "lobby.started")

data class LobbyEventType(val type: String)

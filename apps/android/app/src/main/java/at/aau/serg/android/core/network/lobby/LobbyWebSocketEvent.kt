package at.aau.serg.android.core.network.lobby

import shared.models.lobby.response.LobbyResponse

data class LobbyUpdatedPayload(
    val type: String,
    val lobby: LobbyResponse
)

data class LobbyDeletedPayload(
    val type: String,
    val lobbyId: String
)

data class LobbyStartedPayload(
    val type: String,
    val lobbyId: String,
    val matchId: String
)

data class LobbyEventType(val type: String)

package at.aau.serg.android.core.network.lobby

import shared.models.lobby.response.LobbyResponse

data class AuthenticatedLobbyResponse(
    val accessToken: String,
    val lobby: LobbyResponse
)

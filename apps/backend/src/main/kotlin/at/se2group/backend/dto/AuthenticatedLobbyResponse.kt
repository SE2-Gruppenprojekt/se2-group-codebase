package at.se2group.backend.dto

data class AuthenticatedLobbyResponse(
    val accessToken: String,
    val lobby: LobbyResponse
)

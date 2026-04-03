package at.se2group.backend.dto

data class LobbyListItemResponse(
    val lobbyId: String,
    val hostUserId: String,
    val status: String,
    val currentPlayerCount: Int,
    val maxPlayers: Int,
    val isPrivate: Boolean
)

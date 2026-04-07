package shared.models.lobby.response

data class LobbyResponse(
    val lobbyId: String,
    val hostUserId: String,
    val status: String,
    val players: List<LobbyPlayerResponse>,
    val maxPlayers: Int,
    val isPrivate: Boolean,
    val allowGuests: Boolean
)

package shared.models.lobby.request

data class CreateLobbyRequest(
    val displayName: String,
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true,
    val requireInitialMeld: Boolean = true,
    val startingTiles: Int = 14
)

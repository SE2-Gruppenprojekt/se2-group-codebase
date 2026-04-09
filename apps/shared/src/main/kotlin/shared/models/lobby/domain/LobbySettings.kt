package shared.models.lobby.domain

data class LobbySettings(
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true
)

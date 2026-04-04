package at.se2group.backend.dto

data class CreateLobbyRequest(
    val maxPlayers: Int = 4,
    val isPrivate: Boolean = false,
    val allowGuests: Boolean = true
)

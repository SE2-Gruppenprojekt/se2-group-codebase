package at.se2group.backend.dto

data class UpdateLobbySettingsRequest(
    val maxPlayers: Int,
    val isPrivate: Boolean,
    val allowGuests: Boolean
)

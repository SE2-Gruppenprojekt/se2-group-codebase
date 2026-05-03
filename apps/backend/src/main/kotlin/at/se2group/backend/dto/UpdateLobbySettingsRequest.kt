package at.se2group.backend.dto

import jakarta.validation.constraints.*

data class UpdateLobbySettingsRequest(

    @field:Min(2, message = "maxPlayers must be at least 2")
    @field:Max(8, message = "maxPlayers must not exceed 8")
    val maxPlayers: Int,

    val isPrivate: Boolean,
    val allowGuests: Boolean
)

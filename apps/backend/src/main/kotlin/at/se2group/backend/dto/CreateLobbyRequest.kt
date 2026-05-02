package at.se2group.backend.dto

import jakarta.validation.constraints.*

data class CreateLobbyRequest(

@field:NotBlank(message = "displayName must not be blank")
val displayName: String,

@field:Min(2, message = "maxPlayers must be at least 2")
@field:Max(8, message = "maxPlayers must not exceed 8")
val maxPlayers: Int = 4,

val isPrivate: Boolean = false,
val allowGuests: Boolean = true
)

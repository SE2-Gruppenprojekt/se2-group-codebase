package at.se2group.backend.dto

import jakarta.validation.constraints.NotBlank

data class JoinLobbyRequest(
    @field:NotBlank(message = "displayName must not be blank")
    val displayName: String
)

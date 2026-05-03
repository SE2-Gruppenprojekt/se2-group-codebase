package at.se2group.backend.dto

import jakarta.validation.constraints.NotBlank

data class JoinLobbyRequest(

    @field:NotBlank(message = "userId must not be blank")
    val userId: String,

    @field:NotBlank(message = "displayName must not be blank")
    val displayName: String
)

package at.se2group.backend.dto

data class LobbyUpdatedEvent(
    val type: String = "lobby.updated",
    val lobby: LobbyResponse
)

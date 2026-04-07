package at.se2group.backend.dto

data class LobbyStartedEvent(
    val type: String = "lobby.started",
    val lobbyId: String,
    val matchId: String
)

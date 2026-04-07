package at.se2group.backend.dto

data class LobbyDeletedEvent(
    val type: String = "lobby.deleted",
    val lobbyId: String
)

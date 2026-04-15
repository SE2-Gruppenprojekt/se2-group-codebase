package shared.models.lobby.response

data class LobbyPlayerResponse(
    val userId: String,
    val displayName: String,
    val isReady: Boolean
)

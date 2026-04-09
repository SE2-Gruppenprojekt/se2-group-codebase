package shared.models.lobby.request

data class JoinLobbyRequest(
    val userId: String,
    val displayName: String
)

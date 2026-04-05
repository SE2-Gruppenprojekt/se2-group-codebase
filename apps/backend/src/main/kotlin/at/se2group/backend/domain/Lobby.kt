package at.se2group.backend.domain

import java.time.Instant

data class Lobby(
    val lobbyId: String,
    val hostUserId: String,
    val players: List<LobbyPlayer>,
    val status: LobbyStatus,
    val settings: LobbySettings,
    val createdAt: Instant
)

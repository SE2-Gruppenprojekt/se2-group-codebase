package at.se2group.backend.domain

import java.time.Instant

data class LobbyPlayer(
    val userId: String,
    val displayName: String,
    val isReady: Boolean = false,
    val joinedAt: Instant = Instant.now()
)

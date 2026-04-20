package at.se2group.backend.domain

import java.time.Instant

data class GamePlayer(
    val userId: String,
    val displayName: String,
    val turnOrder: Int,
    val rackTiles: List<Tile> = emptyList(),
    val hasCompletedInitialMeld: Boolean = false,
    val score: Int = 0,
    val joinedAt: Instant = Instant.now()
) {
    init {
        require(userId.isNotBlank()) { "userId must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(turnOrder >= 0) { "turnOrder must not be negative" }
    }
}

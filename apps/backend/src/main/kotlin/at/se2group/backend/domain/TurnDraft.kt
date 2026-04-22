package at.se2group.backend.domain

import java.time.Instant

data class TurnDraft(
    val gameId: String,
    val playerUserId: String,
    val boardSets: List<BoardSet> = emptyList(),
    val rackTiles: List<Tile> = emptyList(),
    val drawnTile: Tile? = null,
    val status: TurnDraftStatus = TurnDraftStatus.IN_PROGRESS,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt
) {
    init {
        require(gameId.isNotBlank()) { "gameId must not be blank" }
        require(playerUserId.isNotBlank()) { "playerUserId must not be blank" }
        require(!updatedAt.isBefore(createdAt)) { "updatedAt must not be before createdAt" }
    }
}

package at.se2group.backend.dto

import java.time.Instant

data class GameResponse(
    val gameId: String,
    val lobbyId: String,
    val players: List<GamePlayerResponse>,
    val boardSets: List<BoardSetResponse>,
    val drawPile: List<TileResponse>,
    val currentPlayerUserId: String,
    val status: String,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?
)

package shared.models.game.response

import java.time.Instant

data class GameResponse(
    val gameId: String,
    val lobbyId: String,
    val players: List<GamePlayerResponse>,
    val board: List<BoardSetResponse>,
    val drawPile: List<TileResponse>,
    val drawPileCount: Int,
    val currentPlayerUserId: String,
    val currentTurnPlayerId: String,
    val turnDeadline: Instant?,
    val remainingTurnSeconds: Int?,
    val status: String,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?
)

package shared.models.game.response

data class GameResponse(
    val gameId: String,
    val lobbyId: String,
    val players: List<GamePlayerResponse>,
    val board: List<BoardSetResponse>,
    val drawPile: List<TileResponse>,
    val drawPileCount: Int,
    val currentPlayerUserId: String,
    val currentTurnPlayerId: String,
    val turnDeadline: String?,
    val remainingTurnSeconds: Int?,
    val status: String,
    val createdAt: String,
    val startedAt: String?,
    val finishedAt: String?
)

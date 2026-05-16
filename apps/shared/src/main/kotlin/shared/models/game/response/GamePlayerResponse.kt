package shared.models.game.response

data class GamePlayerResponse(
    val userId: String,
    val displayName: String,
    val turnOrder: Int,
    val rackTiles: List<TileResponse>,
    val hasCompletedInitialMeld: Boolean,
    val score: Int,
    val joinedAt: String
)

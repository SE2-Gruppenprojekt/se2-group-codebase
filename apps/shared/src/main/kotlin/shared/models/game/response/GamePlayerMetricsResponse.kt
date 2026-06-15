package shared.models.game.response

data class GamePlayerMetricsResponse(
    val turnsCompleted: Int,
    val tilesPlayed: Int,
    val meldsCreated: Int,
    val pointsPlayed: Int,
    val tilesRemainingAtEnd: Int?,
    val penaltyPointsAtEnd: Int?,
    val winner: Boolean,
    val finishPosition: Int?
)

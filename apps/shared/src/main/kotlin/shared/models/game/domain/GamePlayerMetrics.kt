package shared.models.game.domain

data class GamePlayerMetrics(
    val turnsCompleted: Int = 0,
    val tilesPlayed: Int = 0,
    val meldsCreated: Int = 0,
    val pointsPlayed: Int = 0,
    val tilesRemainingAtEnd: Int? = null,
    val penaltyPointsAtEnd: Int? = null,
    val winner: Boolean = false,
    val finishPosition: Int? = null
)

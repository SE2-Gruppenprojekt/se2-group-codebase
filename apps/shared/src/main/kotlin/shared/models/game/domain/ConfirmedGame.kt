package shared.models.game.domain

import java.time.Instant

data class ConfirmedGame(
    val gameId: String,
    val lobbyId: String,
    val players: List<GamePlayer>,
    val boardSets: List<BoardSet> = emptyList(),
    val drawPile: List<Tile> = emptyList(),
    val currentPlayerUserId: String,
    val status: GameStatus = GameStatus.WAITING,
    val requireInitialMeld: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val totalTurnsCompleted: Int = 0,
    val requireInitialMeld: Boolean = false,
    val winnerUserId: String? = null
) {
    init {
        require(gameId.isNotBlank()) { "gameId must not be blank" }
        require(lobbyId.isNotBlank()) { "lobbyId must not be blank" }
        require(players.isNotEmpty()) { "games must contain at least one player" }
        require(currentPlayerUserId.isNotBlank()) { "currentPlayerUserId must not be blank" }
        require(players.any { it.userId == currentPlayerUserId }) {
            "currentPlayerUserId must belong to one of the game players"
        }
    }
}

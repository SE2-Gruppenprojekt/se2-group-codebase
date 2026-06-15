package at.se2group.backend.service

import org.springframework.stereotype.Service
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import java.time.Instant

/**
 * Calculates and aggregates player and game metrics used for post-game
 * statistics and result screens.
 *
 * Metrics are updated incrementally when a turn is committed and finalized
 * once a game has ended.
 */
@Service
class GameMetricsService {
    /**
     * Updates gameplay metrics for the player who committed a valid turn.
     *
     * Tracks:
     * - completed turns
     * - tiles played from the rack
     * - newly created board sets
     * - points contributed by played tiles
     */
    fun applyCommittedTurnMetrics(
        confirmedBeforeTurn: ConfirmedGame,
        committedGame: ConfirmedGame,
        actingPlayerUserId: String
    ): ConfirmedGame {
        val beforePlayer = confirmedBeforeTurn.players.first { it.userId == actingPlayerUserId }
        val afterPlayer = committedGame.players.first { it.userId == actingPlayerUserId }

        val afterRackTileIds = afterPlayer.rackTiles.map { it.tileId }.toSet()
        // Tiles played during the turn are determined by comparing the acting
        // player's rack before and after the committed draft.
        val playedTiles = beforePlayer.rackTiles.filter { it.tileId !in afterRackTileIds }

        val previousBoardSetIds = confirmedBeforeTurn.boardSets.map { it.boardSetId }.toSet()

        // A meld is counted only when a new board set is introduced.
        // Rearranging or extending existing board sets does not increase this metric.
        val meldsCreated = committedGame.boardSets.count { it.boardSetId !in previousBoardSetIds }

        val updatedPlayers = committedGame.players.map { player ->
            if (player.userId != actingPlayerUserId) {
                player
            } else {
                player.copy(
                    metrics = player.metrics.copy(
                        turnsCompleted = player.metrics.turnsCompleted + 1,
                        tilesPlayed = player.metrics.tilesPlayed + playedTiles.size,
                        meldsCreated = player.metrics.meldsCreated + meldsCreated,
                        pointsPlayed = player.metrics.pointsPlayed + playedTiles.sumOf { metricPointValue(it) }
                    )
                )
            }
        }

        return committedGame.copy(
            players = updatedPlayers,
            totalTurnsCompleted = committedGame.totalTurnsCompleted + 1
        )
    }

    /**
     * Computes end-game metrics and final rankings.
     *
     * Determines the winner, calculates remaining rack penalties, assigns finish
     * positions and populates result-screen statistics.
     */
    fun finalizeEndGameMetrics(game: ConfirmedGame): ConfirmedGame {
        val winnerUserId = game.winnerUserId ?: determineWinnerUserId(game)

        // Winner always ranks first. Remaining players are ranked by ascending
        // penalty points with turn order used as a deterministic tie-breaker.
        val rankedPlayers = game.players
            .sortedWith(
                compareBy<GamePlayer> { if (it.userId == winnerUserId) 0 else 1 }
                    .thenBy { it.rackTiles.sumOf(::tilePenaltyValue) }
                    .thenBy { it.turnOrder }
            )

        val finishPositionsByUserId = rankedPlayers
            .mapIndexed { index, player -> player.userId to index + 1 }
            .toMap()

        val updatedPlayers = game.players.map { player ->
            val penalty = player.rackTiles.sumOf(::tilePenaltyValue)

            player.copy(
                metrics = player.metrics.copy(
                    tilesRemainingAtEnd = player.rackTiles.size,
                    penaltyPointsAtEnd = penalty,
                    winner = player.userId == winnerUserId,
                    finishPosition = finishPositionsByUserId[player.userId]
                )
            )
        }

        return game.copy(
            players = updatedPlayers,
            winnerUserId = winnerUserId,
            finishedAt = game.finishedAt ?: Instant.now()
        )
    }

    /**
     * Resolves the winner of a finished game.
     *
     * A finished game is expected to either explicitly store a winner or have a
     * player with an empty rack from which the winner can be derived.
     */
    private fun determineWinnerUserId(game: ConfirmedGame): String {
        return requireNotNull(
            game.winnerUserId
                ?: game.players.firstOrNull { it.rackTiles.isEmpty() }?.userId
        ) {
            "Expected finished game to have either winnerUserId set or a player with an empty rack"
        }
    }

    /**
     * Point value used for gameplay metrics (e.g. pointsPlayed).
     * Jokers contribute 0 points because they do not have an intrinsic number value.
     */
    private fun metricPointValue(tile: Tile): Int =
        when (tile) {
            is NumberedTile -> tile.number
            is JokerTile -> 0
        }

    /**
     * Penalty value used for end-game scoring.
     * Jokers count as 30 penalty points according to the game rules.
     */
    private fun tilePenaltyValue(tile: Tile): Int =
        when (tile) {
            is NumberedTile -> tile.number
            is JokerTile -> 30
        }
}

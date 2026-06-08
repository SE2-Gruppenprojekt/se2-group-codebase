package at.se2group.backend.game.service

import at.se2group.backend.service.GameMetricsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import shared.models.game.domain.BoardSet
import shared.models.game.domain.BoardSetType
import shared.models.game.domain.ConfirmedGame
import shared.models.game.domain.GamePlayer
import shared.models.game.domain.JokerTile
import shared.models.game.domain.NumberedTile
import shared.models.game.domain.Tile
import shared.models.game.domain.TileColor

class GameMetricsServiceTest {
    private val service = GameMetricsService()

    @Test
    fun `applyCommittedTurnMetrics increments acting player turn metrics`() {
        val tile1 = numbered("tile-1", 3)
        val tile2 = numbered("tile-2", 4)
        val remainingTile = numbered("tile-3", 9)

        val before = game(
            players = listOf(
                player("user-1", 0, rackTiles = listOf(tile1, tile2, remainingTile)),
                player("user-2", 1, rackTiles = listOf(numbered("tile-4", 8)))
            ),
            boardSets = listOf(boardSet("set-old", listOf(numbered("tile-5", 10))))
        )

        val committed = before.copy(
            players = listOf(
                player("user-1", 0, rackTiles = listOf(remainingTile)),
                player("user-2", 1, rackTiles = listOf(numbered("tile-4", 8)))
            ),
            boardSets = before.boardSets + boardSet("set-new", listOf(tile1, tile2, numbered("tile-6", 5)))
        )

        val result = service.applyCommittedTurnMetrics(
            confirmedBeforeTurn = before,
            committedGame = committed,
            actingPlayerUserId = "user-1"
        )

        val actingPlayer = result.players.first { it.userId == "user-1" }
        val otherPlayer = result.players.first { it.userId == "user-2" }

        assertEquals(1, result.totalTurnsCompleted)
        assertEquals(1, actingPlayer.metrics.turnsCompleted)
        assertEquals(2, actingPlayer.metrics.tilesPlayed)
        assertEquals(1, actingPlayer.metrics.meldsCreated)
        assertEquals(7, actingPlayer.metrics.pointsPlayed)

        assertEquals(0, otherPlayer.metrics.turnsCompleted)
        assertEquals(0, otherPlayer.metrics.tilesPlayed)
        assertEquals(0, otherPlayer.metrics.meldsCreated)
        assertEquals(0, otherPlayer.metrics.pointsPlayed)
    }

    @Test
    fun `applyCommittedTurnMetrics does not count board rearrangement as tile placement`() {
        val rackTile = numbered("rack-tile", 6)
        val boardTile = numbered("board-tile", 7)

        val before = game(
            players = listOf(
                player("user-1", 0, rackTiles = listOf(rackTile)),
                player("user-2", 1)
            ),
            boardSets = listOf(boardSet("set-1", listOf(boardTile, numbered("tile-2", 8), numbered("tile-3", 9))))
        )

        val committed = before.copy(
            boardSets = listOf(boardSet("set-1", listOf(numbered("tile-2", 8), boardTile, numbered("tile-3", 9))))
        )

        val result = service.applyCommittedTurnMetrics(
            confirmedBeforeTurn = before,
            committedGame = committed,
            actingPlayerUserId = "user-1"
        )

        val actingPlayer = result.players.first { it.userId == "user-1" }

        assertEquals(1, actingPlayer.metrics.turnsCompleted)
        assertEquals(0, actingPlayer.metrics.tilesPlayed)
        assertEquals(0, actingPlayer.metrics.pointsPlayed)
        assertEquals(0, actingPlayer.metrics.meldsCreated)
    }

    @Test
    fun `applyCommittedTurnMetrics counts joker as zero points played`() {
        val joker = JokerTile("joker-1", TileColor.RED)
        val numbered = numbered("tile-1", 12)

        val before = game(
            players = listOf(
                player("user-1", 0, rackTiles = listOf(joker, numbered)),
                player("user-2", 1)
            )
        )

        val committed = before.copy(
            players = listOf(
                player("user-1", 0, rackTiles = emptyList()),
                player("user-2", 1)
            ),
            boardSets = listOf(boardSet("set-1", listOf(joker, numbered, numbered("tile-2", 13))))
        )

        val result = service.applyCommittedTurnMetrics(
            confirmedBeforeTurn = before,
            committedGame = committed,
            actingPlayerUserId = "user-1"
        )

        val actingPlayer = result.players.first { it.userId == "user-1" }

        assertEquals(2, actingPlayer.metrics.tilesPlayed)
        assertEquals(12, actingPlayer.metrics.pointsPlayed)
    }

    @Test
    fun `finalizeEndGameMetrics marks empty rack player as winner`() {
        val game = game(
            players = listOf(
                player("user-1", 0, rackTiles = emptyList()),
                player("user-2", 1, rackTiles = listOf(numbered("tile-1", 5), JokerTile("joker-1", TileColor.BLACK)))
            ),
            currentPlayerUserId = "user-1"
        )

        val result = service.finalizeEndGameMetrics(game)

        val winner = result.players.first { it.userId == "user-1" }
        val loser = result.players.first { it.userId == "user-2" }

        assertEquals("user-1", result.winnerUserId)
        assertTrue(winner.metrics.winner)
        assertEquals(1, winner.metrics.finishPosition)
        assertEquals(0, winner.metrics.tilesRemainingAtEnd)
        assertEquals(0, winner.metrics.penaltyPointsAtEnd)

        assertFalse(loser.metrics.winner)
        assertEquals(2, loser.metrics.finishPosition)
        assertEquals(2, loser.metrics.tilesRemainingAtEnd)
        assertEquals(35, loser.metrics.penaltyPointsAtEnd)
    }

    @Test
    fun `finalizeEndGameMetrics keeps existing winner user id`() {
        val game = game(
            players = listOf(
                player("user-1", 0, rackTiles = emptyList()),
                player("user-2", 1, rackTiles = listOf(numbered("tile-1", 5)))
            ),
            currentPlayerUserId = "user-1",
            winnerUserId = "user-2"
        )

        val result = service.finalizeEndGameMetrics(game)

        assertEquals("user-2", result.winnerUserId)
        assertFalse(result.players.first { it.userId == "user-1" }.metrics.winner)
        assertTrue(result.players.first { it.userId == "user-2" }.metrics.winner)
    }

    @Test
    fun `finalizeEndGameMetrics ranks non winners by lowest penalty then turn order`() {
        val game = game(
            players = listOf(
                player("winner", 0, rackTiles = emptyList()),
                player("user-2", 1, rackTiles = listOf(numbered("tile-1", 10))),
                player("user-3", 2, rackTiles = listOf(numbered("tile-2", 3))),
                player("user-4", 3, rackTiles = listOf(numbered("tile-3", 3)))
            ),
            currentPlayerUserId = "winner"
        )

        val result = service.finalizeEndGameMetrics(game)

        assertEquals(1, result.players.first { it.userId == "winner" }.metrics.finishPosition)
        assertEquals(4, result.players.first { it.userId == "user-2" }.metrics.finishPosition)
        assertEquals(2, result.players.first { it.userId == "user-3" }.metrics.finishPosition)
        assertEquals(3, result.players.first { it.userId == "user-4" }.metrics.finishPosition)
    }

    private fun game(
        players: List<GamePlayer>,
        boardSets: List<BoardSet> = emptyList(),
        currentPlayerUserId: String = "user-1",
        winnerUserId: String? = null
    ) = ConfirmedGame(
        gameId = "game-1",
        lobbyId = "lobby-1",
        players = players,
        boardSets = boardSets,
        currentPlayerUserId = currentPlayerUserId,
        winnerUserId = winnerUserId
    )

    private fun player(
        userId: String,
        turnOrder: Int,
        rackTiles: List<Tile> = emptyList()
    ) = GamePlayer(
        userId = userId,
        displayName = userId,
        turnOrder = turnOrder,
        rackTiles = rackTiles
    )

    private fun boardSet(
        boardSetId: String,
        tiles: List<Tile>
    ) = BoardSet(
        boardSetId = boardSetId,
        type = BoardSetType.RUN,
        tiles = tiles
    )

    private fun numbered(
        tileId: String,
        number: Int
    ) = NumberedTile(
        tileId = tileId,
        color = TileColor.BLUE,
        number = number
    )

}
